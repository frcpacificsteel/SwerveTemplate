package frc.robot.config;

/** Limelight settings that are independent of the current season's field layout. */
public final class VisionConstants {
  public static final String LIMELIGHT_NAME = "limelight";
  public static final double MAX_ANGULAR_RATE_RPS = 2.0;
  public static final double SINGLE_TAG_MAX_DISTANCE_METERS = 4.0;
  public static final double HEARTBEAT_TIMEOUT_SECONDS = 1.0;
  public static final double TRUSTED_POSE_MAX_AGE_SECONDS = 0.5;

  private VisionConstants() {}
}
