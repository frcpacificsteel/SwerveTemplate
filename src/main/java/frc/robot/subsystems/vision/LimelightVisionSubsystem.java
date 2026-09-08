package frc.robot.subsystems.vision;

import com.ctre.phoenix6.swerve.SwerveDrivetrain.SwerveDriveState;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringPublisher;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.LimelightHelpers;
import frc.robot.config.VisionConstants;
import frc.robot.subsystems.drivetrain.CommandSwerveDrivetrain;
import frc.robot.telemetry.Tunables;
import frc.robot.util.FieldUtil;

/**
 * The only subsystem that communicates with the separate Limelight camera.
 * Commands consume the drivetrain pose and vision health, not LimelightHelpers.
 */
public class LimelightVisionSubsystem extends SubsystemBase {
  private static final Matrix<N3, N1> SINGLE_TAG_STD_DEVS = VecBuilder.fill(1.0, 1.0, Math.toRadians(20));
  private static final Matrix<N3, N1> MULTI_TAG_STD_DEVS = VecBuilder.fill(0.3, 0.3, Math.toRadians(5));

  private static final BooleanPublisher TARGET_VISIBLE = NetworkTableInstance.getDefault()
      .getTable("Vision")
      .getBooleanTopic("targetVisible")
      .publish();
  private static final BooleanPublisher TRUSTED_POSE = NetworkTableInstance.getDefault()
      .getTable("Vision")
      .getBooleanTopic("trustedPose")
      .publish();
  private static final DoublePublisher TAG_COUNT = NetworkTableInstance.getDefault()
      .getTable("Vision")
      .getDoubleTopic("tagCount")
      .publish();
  private static final StringPublisher REJECTION_REASON = NetworkTableInstance.getDefault()
      .getTable("Vision")
      .getStringTopic("rejectionReason")
      .publish();

  private final CommandSwerveDrivetrain drivetrain;
  private VisionEstimate lastEstimate = VisionEstimate.unavailable("No estimate received");
  private boolean targetVisible;
  private double lastHeartbeatValue = Double.NaN;
  private double lastHeartbeatTimeSeconds = Double.NEGATIVE_INFINITY;
  private double lastTrustedPoseTimeSeconds = Double.NEGATIVE_INFINITY;

  public LimelightVisionSubsystem(CommandSwerveDrivetrain drivetrain) {
    this.drivetrain = drivetrain;
  }

  public boolean hasTrustedPose() {
    return lastEstimate.trusted();
  }

  public boolean isConnected() {
    return Timer.getFPGATimestamp() - lastHeartbeatTimeSeconds
        <= VisionConstants.HEARTBEAT_TIMEOUT_SECONDS;
  }

  public boolean hasRecentTrustedPose() {
    return Timer.getFPGATimestamp() - lastTrustedPoseTimeSeconds
        <= VisionConstants.TRUSTED_POSE_MAX_AGE_SECONDS;
  }

  public VisionEstimate getLastEstimate() {
    return lastEstimate;
  }

  public VisionHealth getHealth() {
    return new VisionHealth(isConnected(), targetVisible, hasRecentTrustedPose());
  }

  @Override
  public void periodic() {
    updateHeartbeat();
    SwerveDriveState driveState = drivetrain.getState();
    double headingDegrees = driveState.Pose.getRotation().getDegrees();
    double omegaRps = Units.radiansToRotations(driveState.Speeds.omegaRadiansPerSecond);
    LimelightHelpers.SetRobotOrientation(VisionConstants.LIMELIGHT_NAME, headingDegrees, 0, 0, 0, 0, 0);

    targetVisible = LimelightHelpers.getTV(VisionConstants.LIMELIGHT_NAME);
    TARGET_VISIBLE.set(targetVisible);

    var estimate = LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2(VisionConstants.LIMELIGHT_NAME);
    lastEstimate = evaluate(estimate, omegaRps);
    TRUSTED_POSE.set(lastEstimate.trusted());
    TAG_COUNT.set(lastEstimate.tagCount());
    REJECTION_REASON.set(lastEstimate.rejectionReason());

    if (lastEstimate.trusted()) {
      lastTrustedPoseTimeSeconds = Timer.getFPGATimestamp();
      drivetrain.addVisionMeasurement(
          lastEstimate.pose(),
          lastEstimate.timestampSeconds(),
          lastEstimate.tagCount() > 1 ? MULTI_TAG_STD_DEVS : SINGLE_TAG_STD_DEVS);
    }
  }

  private void updateHeartbeat() {
    double heartbeat = LimelightHelpers
        .getLimelightNTTableEntry(VisionConstants.LIMELIGHT_NAME, "hb")
        .getDouble(Double.NaN);
    if (Double.isFinite(heartbeat) && heartbeat != lastHeartbeatValue) {
      lastHeartbeatValue = heartbeat;
      lastHeartbeatTimeSeconds = Timer.getFPGATimestamp();
    }
  }

  private VisionEstimate evaluate(LimelightHelpers.PoseEstimate estimate, double omegaRps) {
    if (estimate == null) {
      return VisionEstimate.unavailable("No pose estimate");
    }
    if (estimate.tagCount <= 0) {
      return fromLimelight(estimate, false, "No AprilTags in estimate");
    }
    if (!Double.isFinite(estimate.timestampSeconds)) {
      return fromLimelight(estimate, false, "Invalid timestamp");
    }
    if (Math.abs(omegaRps) > Tunables.VISION_MAX_ANGULAR_RATE_RPS.get()) {
      return fromLimelight(estimate, false, "Robot rotating too quickly");
    }
    if (!FieldUtil.isInsideField(estimate.pose)) {
      return fromLimelight(estimate, false, "Pose outside field bounds");
    }
    if (estimate.tagCount == 1
        && estimate.avgTagDist > Tunables.VISION_SINGLE_TAG_MAX_DISTANCE_METERS.get()) {
      return fromLimelight(estimate, false, "Single tag is too far away");
    }
    return fromLimelight(estimate, true, "");
  }

  private VisionEstimate fromLimelight(
      LimelightHelpers.PoseEstimate estimate,
      boolean trusted,
      String rejectionReason) {
    return new VisionEstimate(
        estimate.pose,
        estimate.timestampSeconds,
        estimate.tagCount,
        estimate.avgTagDist,
        trusted,
        rejectionReason);
  }
}
