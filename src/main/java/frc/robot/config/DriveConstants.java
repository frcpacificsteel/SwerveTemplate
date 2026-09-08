package frc.robot.config;

/** High-frequency drive and pathfinding adjustments. Detailed swerve calibration stays in SwerveConstants. */
public final class DriveConstants {
  public static final double TELEOP_SPEED_SCALE = 1.00;
  public static final double MAX_ANGULAR_RATE_RPS = 0.75;
  public static final double DRIVE_DEADBAND_PERCENT = 0.10;

  public static final double PATH_MAX_LINEAR_SPEED_MPS = 3.0;
  public static final double PATH_MAX_LINEAR_ACCEL_MPS_SQ = 3.0;
  public static final double PATH_MAX_ANGULAR_SPEED_DEG_PER_SEC = 540.0;
  public static final double PATH_MAX_ANGULAR_ACCEL_DEG_PER_SEC_SQ = 720.0;

  private DriveConstants() {}
}
