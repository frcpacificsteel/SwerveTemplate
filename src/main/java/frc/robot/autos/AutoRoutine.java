package frc.robot.autos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.config.AutoConstants;

/**
 * Fluent builder for Java-defined autos. Branch conditions are evaluated when the
 * step is reached, so route choice can react to the actual time already spent.
 */
public final class AutoRoutine {
  private record Step(String name, java.util.function.Function<AutoRuntime, Command> commandFactory) {}

  private AutoRoutine() {}

  public static Builder named(String name) {
    return new Builder(name);
  }

  public static final class Builder {
    private final String name;
    private final List<Step> steps = new ArrayList<>();
    private final List<AutoCheck> checks = new ArrayList<>();

    private Builder(String name) {
      this.name = name;
    }

    public Builder step(String stepName, Supplier<Command> command) {
      steps.add(new Step(stepName, ignored -> command.get()));
      return this;
    }

    public Builder pathfind(String stepName, AutoFactory auto, Pose2d blueOriginTarget) {
      return step(stepName, () -> auto.pathfindTo(blueOriginTarget));
    }

    /** Follows a saved `.path` file; this routine is unavailable if the file is missing. */
    public Builder followPath(String stepName, AutoFactory auto, String pathName) {
      checks.add(AutoCheck.require(
          () -> auto.savedPathExists(pathName),
          "Missing saved path: " + pathName));
      return step(stepName, () -> auto.followPath(pathName));
    }

    /** Recovers with pathfinding, then joins and follows a saved `.path` file. */
    public Builder pathfindThenFollowPath(String stepName, AutoFactory auto, String pathName) {
      checks.add(AutoCheck.require(
          () -> auto.savedPathExists(pathName),
          "Missing saved path: " + pathName));
      return step(stepName, () -> auto.pathfindThenFollowPath(pathName));
    }

    public Builder waitSeconds(String stepName, AutoFactory auto, double seconds) {
      return step(stepName, () -> auto.waitSeconds(seconds));
    }

    /** Selects a route from live robot state when this step begins. */
    public Builder when(
        String stepName,
        BooleanSupplier condition,
        Supplier<Command> whenTrue,
        Supplier<Command> whenFalse) {
      steps.add(new Step(stepName, ignored -> Commands.either(whenTrue.get(), whenFalse.get(), condition)));
      return this;
    }

    /**
     * Chooses the full route only when enough real match time remains. Otherwise
     * the short route runs. Choose the threshold from on-robot measurements plus margin.
     */
    public Builder whenTimeRemaining(
        String stepName,
        double minimumRemainingSeconds,
        Supplier<Command> fullRoute,
        Supplier<Command> shortRoute) {
      steps.add(new Step(
          stepName,
          runtime -> Commands.either(
              fullRoute.get(),
              shortRoute.get(),
              () -> runtime.remainingSeconds(AutoConstants.MATCH_DURATION_SECONDS)
                  >= minimumRemainingSeconds)));
      return this;
    }

    /**
     * Re-evaluates scoring feasibility from the robot's live pose when this step
     * begins. This is safer than using one fixed time threshold for every field position.
     */
    public Builder whenScoringStillAvailable(
        String stepName,
        AutoFactory auto,
        Pose2d blueOriginScoringPose,
        double scoringActionSeconds,
        double safetyMarginSeconds,
        Supplier<Command> scoringRoute,
        Supplier<Command> alternateRoute) {
      steps.add(new Step(
          stepName,
          runtime -> Commands.either(
              scoringRoute.get(),
              alternateRoute.get(),
              () -> auto.canReachAndScore(
                  blueOriginScoringPose,
                  runtime.remainingSeconds(AutoConstants.MATCH_DURATION_SECONDS),
                  scoringActionSeconds,
                  safetyMarginSeconds))));
      return this;
    }

    public Builder require(BooleanSupplier condition, String failureMessage) {
      checks.add(AutoCheck.require(condition, failureMessage));
      return this;
    }

    public AutoPlan build() {
      return AutoPlan.of(name, () -> {
        AutoRuntime runtime = new AutoRuntime(name);
        List<Command> commands = new ArrayList<>();
        commands.add(Commands.runOnce(runtime::start));
        for (Step step : steps) {
          commands.add(Commands.sequence(
              Commands.runOnce(() -> runtime.setStep(step.name())),
              step.commandFactory().apply(runtime)));
        }
        return Commands.sequence(commands.toArray(Command[]::new));
      }, checks);
    }
  }
}
