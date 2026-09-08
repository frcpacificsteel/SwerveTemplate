package frc.robot.safety;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringArrayPublisher;
import edu.wpi.first.wpilibj.DriverStation;
import frc.robot.telemetry.RobotAlerts;

/**
 * Reusable mechanism interlocks. A subsystem requests output normally, then
 * gates its output with {@link #isSafe()} so a failed prerequisite cannot move
 * hardware. New mechanisms add their own limit, homing, and health checks here.
 */
public class SafetyManager {
  private record Interlock(String message, BooleanSupplier allowed, boolean overrideable) {}

  private final String name;
  private final Map<String, Interlock> interlocks = new LinkedHashMap<>();
  private final BooleanPublisher safePublisher;
  private final BooleanPublisher overridePublisher;
  private final StringArrayPublisher blockersPublisher;
  private boolean manualOverrideRequested;

  public SafetyManager(String name) {
    this.name = name;
    NetworkTable table = NetworkTableInstance.getDefault().getTable("Safety").getSubTable(name);
    safePublisher = table.getBooleanTopic("safe").publish();
    overridePublisher = table.getBooleanTopic("manualOverrideActive").publish();
    blockersPublisher = table.getStringArrayTopic("blockers").publish();
  }

  public void addInterlock(String key, String message, BooleanSupplier allowed) {
    addHardInterlock(key, message, allowed);
  }

  public void addHardInterlock(String key, String message, BooleanSupplier allowed) {
    interlocks.put(key, new Interlock(message, allowed, false));
  }

  public void addSoftInterlock(String key, String message, BooleanSupplier allowed) {
    interlocks.put(key, new Interlock(message, allowed, true));
  }

  public void requestManualOverride(boolean requested) {
    manualOverrideRequested = requested;
  }

  public boolean isManualOverrideActive() {
    return manualOverrideRequested && DriverStation.isTestEnabled();
  }

  public boolean isSafe() {
    boolean overrideActive = isManualOverrideActive();
    return interlocks.values().stream().allMatch(
        interlock -> interlock.allowed().getAsBoolean() || (interlock.overrideable() && overrideActive));
  }

  public String[] blockers() {
    return interlocks.values().stream()
        .filter(interlock -> !interlock.allowed().getAsBoolean())
        .filter(interlock -> !interlock.overrideable() || !isManualOverrideActive())
        .map(Interlock::message)
        .toArray(String[]::new);
  }

  public void periodic() {
    boolean safe = isSafe();
    boolean overrideActive = isManualOverrideActive();
    safePublisher.set(safe);
    overridePublisher.set(overrideActive);
    blockersPublisher.set(blockers());
    RobotAlerts.setWarning("safety-" + name, name + " BLOCKED: " + String.join(", ", blockers()), !safe);
    RobotAlerts.setWarning("override-" + name, name + " MANUAL SAFETY OVERRIDE ACTIVE", overrideActive);
  }
}
