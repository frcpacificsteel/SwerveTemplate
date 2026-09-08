package frc.robot.telemetry;

import frc.robot.config.VisionConstants;

/** Runtime tuning points. Defaults remain visible and reviewable in config constants. */
public final class Tunables {
  public static final TunableNumber VISION_MAX_ANGULAR_RATE_RPS = new TunableNumber(
      "Vision/MaxAngularRateRps",
      VisionConstants.MAX_ANGULAR_RATE_RPS,
      0.0,
      10.0);
  public static final TunableNumber VISION_SINGLE_TAG_MAX_DISTANCE_METERS = new TunableNumber(
      "Vision/SingleTagMaxDistanceMeters",
      VisionConstants.SINGLE_TAG_MAX_DISTANCE_METERS,
      0.0,
      20.0);

  private Tunables() {}
}
