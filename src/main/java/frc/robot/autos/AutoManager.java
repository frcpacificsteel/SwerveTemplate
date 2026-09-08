package frc.robot.autos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringArrayPublisher;
import edu.wpi.first.networktables.StringPublisher;
import edu.wpi.first.networktables.StringSubscriber;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.config.AutoConstants;
import frc.robot.telemetry.RobotAlerts;

/**
 * A NetworkTables-based autonomous selector that is independent of SmartDashboard
 * and SendableChooser. A dashboard may write Auto/Requested, or controls may use
 * selectNext/selectPrevious while the robot is disabled.
 */
public class AutoManager {
  private final Map<String, AutoPlan> plans = new LinkedHashMap<>();
  private final BooleanSupplier trustedVisionAvailable;
  private final BooleanSupplier superstructureReady;
  private final Supplier<Command> safeStopCommand;
  private final StringSubscriber requestedAuto;
  private final StringPublisher selectedAuto;
  private final StringPublisher readiness;
  private final StringPublisher executingAuto;
  private final StringArrayPublisher availableAutos;
  private String selectedName = "";
  private boolean selectionReady;

  public AutoManager(
      BooleanSupplier trustedVisionAvailable,
      BooleanSupplier superstructureReady,
      Supplier<Command> safeStopCommand) {
    this.trustedVisionAvailable = trustedVisionAvailable;
    this.superstructureReady = superstructureReady;
    this.safeStopCommand = safeStopCommand;
    NetworkTable table = NetworkTableInstance.getDefault().getTable("Auto");
    requestedAuto = table.getStringTopic("Requested").subscribe("");
    selectedAuto = table.getStringTopic("Selected").publish();
    readiness = table.getStringTopic("Readiness").publish();
    executingAuto = table.getStringTopic("Executing").publish();
    availableAutos = table.getStringArrayTopic("Available").publish();
    availableAutos.set(new String[0]);
  }

  public void register(AutoPlan plan) {
    List<String> fallbacks = plan.fallbackNames().isEmpty()
        ? AutoConstants.fallbacksFor(plan.name())
        : plan.fallbackNames();
    AutoPlan configured = plan
        .withFallbacks(fallbacks)
        .withCheck(AutoCheck.require(
            () -> fallbacks.stream().allMatch(plans::containsKey),
            "One or more configured fallback routines do not exist"));
    if (AutoConstants.REQUIRES_TRUSTED_VISION.contains(plan.name())) {
      configured = configured.withCheck(AutoCheck.require(
          trustedVisionAvailable,
          "Recent trusted Limelight pose required"));
    }
    if (AutoConstants.REQUIRES_READY_SUPERSTRUCTURE.contains(plan.name())) {
      configured = configured.withCheck(AutoCheck.require(
          superstructureReady,
          "Superstructure is not ready"));
    }
    BooleanSupplier runtimeCondition = () ->
        (!AutoConstants.ABORTS_ON_VISION_LOSS.contains(plan.name()) || trustedVisionAvailable.getAsBoolean())
            && (!AutoConstants.ABORTS_ON_SUPERSTRUCTURE_FAULT.contains(plan.name())
                || superstructureReady.getAsBoolean());
    String runtimeFailure = AutoConstants.ABORTS_ON_VISION_LOSS.contains(plan.name())
        || AutoConstants.ABORTS_ON_SUPERSTRUCTURE_FAULT.contains(plan.name())
            ? "Runtime requirement lost"
            : "";
    plans.put(plan.name(), configured.withRecoveryPolicy(new AutoRecoveryPolicy(
        runtimeCondition, runtimeFailure, AutoConstants.timeoutFor(plan.name()))));
    availableAutos.set(plans.keySet().toArray(String[]::new));
  }

  public void periodic() {
    String requested = requestedAuto.get();
    if (!requested.isBlank() && plans.containsKey(requested)) {
      selectedName = requested;
    }
    RobotAlerts.setWarning(
        "invalid-auto-request",
        "REQUESTED AUTO DOES NOT EXIST: " + requested,
        !requested.isBlank() && !plans.containsKey(requested));

    publishStatus();
  }

  public void selectNext() {
    selectByOffset(1);
  }

  public void selectPrevious() {
    selectByOffset(-1);
  }

  public Command getSelectedCommand() {
    AutoPlan plan = resolveReadyPlan(plans.get(selectedName));
    executingAuto.set(plan == null ? "" : plan.name());
    return plan == null ? null : buildRecoverableCommand(plan, new HashSet<>());
  }

  public boolean hasReadySelection() {
    return selectionReady;
  }

  private void selectByOffset(int offset) {
    List<String> names = new ArrayList<>(plans.keySet());
    if (names.isEmpty()) {
      return;
    }
    int currentIndex = names.indexOf(selectedName);
    int nextIndex = Math.floorMod(currentIndex + offset, names.size());
    selectedName = names.get(nextIndex);
    publishStatus();
  }

  private void publishStatus() {
    AutoPlan plan = plans.get(selectedName);
    boolean noSelection = plan == null;
    AutoPlan runnablePlan = resolveReadyPlan(plan);
    boolean selectedPlanReady = plan != null && plan.isReady();
    boolean recoveryAvailable = runnablePlan != null && runnablePlan != plan;
    selectionReady = selectedPlanReady;
    String reason = noSelection
        ? "NO AUTO SELECTED"
        : recoveryAvailable
            ? "FALLBACK READY: " + runnablePlan.name()
            : selectedPlanReady ? "READY" : plan.firstFailure();

    selectedAuto.set(noSelection ? "" : plan.name());
    readiness.set(reason);
    RobotAlerts.setWarning("no-auto-selected", "NO AUTO SELECTED — select a validated autonomous routine before the match", noSelection);
    RobotAlerts.setWarning("auto-not-ready", "AUTO NOT READY: " + reason, !noSelection && !selectedPlanReady);
  }

  private AutoPlan resolveReadyPlan(AutoPlan primaryPlan) {
    if (primaryPlan == null) {
      return null;
    }

    var candidates = new ArrayDeque<AutoPlan>();
    candidates.add(primaryPlan);
    var visited = new HashSet<String>();
    while (!candidates.isEmpty()) {
      AutoPlan current = candidates.removeFirst();
      if (!visited.add(current.name())) {
        continue;
      }
      if (current.isReady()) {
        return current;
      }
      current.fallbackNames().stream()
          .map(plans::get)
          .filter(candidate -> candidate != null && !visited.contains(candidate.name()))
          .forEach(candidates::addLast);
    }
    return null;
  }

  private Command buildRecoverableCommand(AutoPlan plan, Set<String> visited) {
    if (!visited.add(plan.name())) {
      return safeStopCommand.get();
    }

    AutoRecoveryPolicy policy = plan.recoveryPolicy();
    if (!policy.hasGuard()) {
      return plan.commandFactory().get();
    }

    AtomicBoolean recoveryRequested = new AtomicBoolean(false);
    AtomicReference<Double> startTime = new AtomicReference<>(0.0);
    Command guarded = plan.commandFactory().get()
        .beforeStarting(() -> {
          executingAuto.set(plan.name());
          startTime.set(Timer.getFPGATimestamp());
        })
        .until(() -> {
          boolean conditionFailed = !policy.continueCondition().getAsBoolean();
          boolean timedOut = policy.timeoutSeconds() > 0.0
              && Timer.getFPGATimestamp() - startTime.get() >= policy.timeoutSeconds();
          recoveryRequested.set(conditionFailed || timedOut);
          return recoveryRequested.get();
        });

    AutoPlan fallback = plan.fallbackNames().stream()
        .map(plans::get)
        .filter(candidate -> candidate != null && candidate.isReady() && !visited.contains(candidate.name()))
        .findFirst()
        .orElse(null);
    Command safeStop = safeStopCommand.get().beforeStarting(() -> executingAuto.set("SAFE STOP"));
    Command recovery = fallback == null
        ? safeStop
        : Commands.either(buildRecoverableCommand(fallback, visited), safeStop, fallback::isReady);

    return guarded.andThen(Commands.either(recovery, Commands.none(), recoveryRequested::get));
  }
}
