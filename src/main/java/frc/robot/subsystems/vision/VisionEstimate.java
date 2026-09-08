package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose2d;

/** Vendor-neutral vision result consumed by drivetrain, autos, and telemetry. */
public record VisionEstimate(
    Pose2d pose,
    double timestampSeconds,
    int tagCount,
    double averageTagDistanceMeters,
    boolean trusted,
    String rejectionReason) {

  public static VisionEstimate unavailable(String reason) {
    return new VisionEstimate(new Pose2d(), 0.0, 0, 0.0, false, reason);
  }
}
