package frc.robot.autos;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringPublisher;
import edu.wpi.first.wpilibj.Timer;

/** Per-run timing and step telemetry supplied to Java-defined autonomous routines. */
public final class AutoRuntime {
  private final String routineName;
  private final StringPublisher stepPublisher = NetworkTableInstance.getDefault()
      .getTable("Auto")
      .getStringTopic("Step")
      .publish();
  private double startedAtSeconds;

  AutoRuntime(String routineName) {
    this.routineName = routineName;
  }

  void start() {
    startedAtSeconds = Timer.getFPGATimestamp();
    stepPublisher.set(routineName + ": starting");
  }

  void setStep(String stepName) {
    stepPublisher.set(routineName + ": " + stepName);
  }

  /** Returns actual elapsed autonomous time, rather than a planned path duration. */
  public double elapsedSeconds() {
    return Timer.getFPGATimestamp() - startedAtSeconds;
  }

  public double remainingSeconds(double autonomousDurationSeconds) {
    return Math.max(0.0, autonomousDurationSeconds - elapsedSeconds());
  }
}
