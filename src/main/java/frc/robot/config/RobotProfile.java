package frc.robot.config;

/** Hardware profiles allow a quick switch without scattering feature flags. */
public enum RobotProfile {
  COMPETITION(true, true),
  PRACTICE(true, true),
  SIMULATION(false, false);

  private final boolean limelightExpected;
  private final boolean operatorControllerExpected;

  RobotProfile(boolean limelightExpected, boolean operatorControllerExpected) {
    this.limelightExpected = limelightExpected;
    this.operatorControllerExpected = operatorControllerExpected;
  }

  public boolean limelightExpected() {
    return limelightExpected;
  }

  public boolean operatorControllerExpected() {
    return operatorControllerExpected;
  }
}
