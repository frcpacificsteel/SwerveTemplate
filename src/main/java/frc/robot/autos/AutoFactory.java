package frc.robot.autos;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.commands.drivetrain.FieldTargetManager;
import frc.robot.superstructure.Superstructure;
import frc.robot.superstructure.SuperstructureGoal;

/** Small vocabulary used by programmers to assemble readable Java autonomous routines. */
public final class AutoFactory {
  private final FieldTargetManager fieldTargets;
  private final Superstructure superstructure;

  public AutoFactory(FieldTargetManager fieldTargets, Superstructure superstructure) {
    this.fieldTargets = fieldTargets;
    this.superstructure = superstructure;
  }

  public AutoRoutine.Builder routine(String name) {
    return AutoRoutine.named(name);
  }

  /** Pathfinds to a blue-origin pose and mirrors it automatically for the red alliance. */
  public Command pathfindTo(Pose2d blueOriginTarget) {
    return fieldTargets.pathfindTo(blueOriginTarget);
  }

  public boolean savedPathExists(String pathName) {
    return fieldTargets.savedPathExists(pathName);
  }

  /** Follows a `.path` file from its intended starting pose. */
  public Command followPath(String pathName) {
    return fieldTargets.followSavedPath(pathName);
  }

  /** Pathfinds from the current pose, then joins and follows a `.path` file. */
  public Command pathfindThenFollowPath(String pathName) {
    return fieldTargets.pathfindThenFollowSavedPath(pathName);
  }

  /** Estimates the remaining time needed to pathfind, act, and retain a safety margin. */
  public boolean canReachAndScore(
      Pose2d blueOriginTarget,
      double remainingAutoSeconds,
      double scoringActionSeconds,
      double safetyMarginSeconds) {
    return remainingAutoSeconds >= fieldTargets.estimatedPathfindTimeSeconds(blueOriginTarget)
        + scoringActionSeconds
        + safetyMarginSeconds;
  }

  public Command goal(SuperstructureGoal goal) {
    return superstructure.requestGoal(goal);
  }

  public Command stow() {
    return superstructure.requestGoal(SuperstructureGoal.STOW);
  }

  public Command waitSeconds(double seconds) {
    return Commands.waitSeconds(seconds);
  }

  public Command sequence(Command... commands) {
    return Commands.sequence(commands);
  }

  public Command parallel(Command... commands) {
    return Commands.parallel(commands);
  }
}
