package frc.robot.commands.drivetrain;

import java.util.Set;
import java.nio.file.Files;
import java.util.function.Function;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathConstraints;
import com.pathplanner.lib.path.PathPlannerPath;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.config.DriveConstants;
import frc.robot.config.FieldConstants;
import frc.robot.subsystems.drivetrain.CommandSwerveDrivetrain;
import frc.robot.telemetry.RobotAlerts;
import frc.robot.util.FieldUtil;

/** Holds a blue-origin target and resolves it for the active alliance at execution time. */
public class FieldTargetManager {
  private final CommandSwerveDrivetrain drivetrain;
  private final PathConstraints constraints = new PathConstraints(
      DriveConstants.PATH_MAX_LINEAR_SPEED_MPS,
      DriveConstants.PATH_MAX_LINEAR_ACCEL_MPS_SQ,
      Units.degreesToRadians(DriveConstants.PATH_MAX_ANGULAR_SPEED_DEG_PER_SEC),
      Units.degreesToRadians(DriveConstants.PATH_MAX_ANGULAR_ACCEL_DEG_PER_SEC_SQ));
  private Pose2d blueTarget = FieldConstants.SCORE_MID;

  public FieldTargetManager(CommandSwerveDrivetrain drivetrain) {
    this.drivetrain = drivetrain;
  }

  public Command selectTarget(Pose2d blueOriginTarget) {
    return Commands.runOnce(() -> blueTarget = blueOriginTarget);
  }

  public Command pathfindToSelectedTarget() {
    return pathfindTo(blueTarget);
  }

  /** Creates an alliance-aware pathfinding command without requiring a saved PathPlanner auto file. */
  public Command pathfindTo(Pose2d blueOriginTarget) {
    return Commands.defer(
        () -> AutoBuilder.pathfindToPose(FieldUtil.forAlliance(blueOriginTarget), constraints, 0.0),
        Set.of(drivetrain));
  }

  public boolean savedPathExists(String pathName) {
    return Files.isRegularFile(Filesystem.getDeployDirectory().toPath()
        .resolve("pathplanner")
        .resolve("paths")
        .resolve(pathName + ".path"));
  }

  /** Follows a saved PathPlanner path exactly as authored. */
  public Command followSavedPath(String pathName) {
    return loadSavedPath(pathName, AutoBuilder::followPath);
  }

  /** Recovers from the robot's current position, then joins and follows the saved path. */
  public Command pathfindThenFollowSavedPath(String pathName) {
    return loadSavedPath(pathName, path -> AutoBuilder.pathfindThenFollowPath(path, constraints));
  }

  /** Conservative travel estimate from the live pose for autonomous route decisions. */
  public double estimatedPathfindTimeSeconds(Pose2d blueOriginTarget) {
    Pose2d target = FieldUtil.forAlliance(blueOriginTarget);
    Pose2d current = drivetrain.getState().Pose;
    double distanceMeters = current.getTranslation().getDistance(target.getTranslation());
    double accelerationTime = DriveConstants.PATH_MAX_LINEAR_SPEED_MPS
        / DriveConstants.PATH_MAX_LINEAR_ACCEL_MPS_SQ;
    double distanceToAccelerateAndBrake = DriveConstants.PATH_MAX_LINEAR_SPEED_MPS * accelerationTime;
    double linearSeconds = distanceMeters <= distanceToAccelerateAndBrake
        ? 2.0 * Math.sqrt(distanceMeters / DriveConstants.PATH_MAX_LINEAR_ACCEL_MPS_SQ)
        : 2.0 * accelerationTime
            + (distanceMeters - distanceToAccelerateAndBrake) / DriveConstants.PATH_MAX_LINEAR_SPEED_MPS;
    double headingDeltaRadians = Math.abs(current.getRotation().minus(target.getRotation()).getRadians());
    double angularSeconds = headingDeltaRadians
        / Math.toRadians(DriveConstants.PATH_MAX_ANGULAR_SPEED_DEG_PER_SEC);
    return Math.max(linearSeconds, angularSeconds);
  }

  private Command loadSavedPath(String pathName, Function<PathPlannerPath, Command> commandFactory) {
    return Commands.defer(() -> {
      try {
        RobotAlerts.setWarning("saved-path-" + pathName, "SAVED PATH UNAVAILABLE: " + pathName, false);
        return commandFactory.apply(PathPlannerPath.fromPathFile(pathName));
      } catch (Exception exception) {
        RobotAlerts.setWarning(
            "saved-path-" + pathName,
            "SAVED PATH UNAVAILABLE: " + pathName + " — " + exception.getMessage(),
            true);
        return Commands.none();
      }
    }, Set.of(drivetrain));
  }
}
