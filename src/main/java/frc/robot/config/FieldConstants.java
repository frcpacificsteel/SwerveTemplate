package frc.robot.config;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

/** Season-specific field positions. Replace these values after the next game reveal. */
public final class FieldConstants {
  public static final double FIELD_LENGTH_METERS = 16.54;
  public static final double FIELD_WIDTH_METERS = 8.07;

  public static final Pose2d SCORE_LEFT = new Pose2d(1.5, 5.1, Rotation2d.fromDegrees(-20));
  public static final Pose2d SCORE_MID = new Pose2d(1.5, 4.0, Rotation2d.fromDegrees(0));
  public static final Pose2d SCORE_RIGHT = new Pose2d(1.5, 2.9, Rotation2d.fromDegrees(20));

  /** Demonstration pose only. Replace after game reveal before enabling the example auto. */
  public static final Pose2d EXAMPLE_AUTO_EXIT = new Pose2d(3.0, 4.0, Rotation2d.kZero);
  /** Replace with a season-specific place the robot can safely defend from after an auto fault. */
  public static final Pose2d DEFENSIVE_SAFE_POSITION = new Pose2d(6.0, 4.0, Rotation2d.kZero);

  private FieldConstants() {}
}
