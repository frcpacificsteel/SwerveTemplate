package frc.robot.util;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.robot.config.FieldConstants;

/** Converts blue-origin field references to the active alliance. */
public final class FieldUtil {
  private FieldUtil() {}

  public static Pose2d forAlliance(Pose2d bluePose) {
    return AllianceUtil.isRedAlliance() ? flipAcrossFieldLength(bluePose) : bluePose;
  }

  public static Pose2d flipAcrossFieldLength(Pose2d pose) {
    return new Pose2d(
        FieldConstants.FIELD_LENGTH_METERS - pose.getX(),
        pose.getY(),
        pose.getRotation().rotateBy(Rotation2d.fromDegrees(180.0)));
  }

  public static boolean isInsideField(Pose2d pose) {
    return pose.getX() >= 0.0
        && pose.getX() <= FieldConstants.FIELD_LENGTH_METERS
        && pose.getY() >= 0.0
        && pose.getY() <= FieldConstants.FIELD_WIDTH_METERS;
  }
}
