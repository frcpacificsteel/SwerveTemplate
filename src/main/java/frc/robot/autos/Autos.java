package frc.robot.autos;

import frc.robot.config.AutoConstants;
import frc.robot.config.FieldConstants;
import frc.robot.superstructure.SuperstructureGoal;

/**
 * The only file programmers normally edit to add autonomous routines.
 * Copy the disabled example, give it a clear name, and replace the field poses.
 */
public final class Autos {
  private Autos() {}

  public static void registerAll(AutoManager autoManager, AutoFactory auto) {
    autoManager.register(auto.routine("Do Nothing").build());

    if (AutoConstants.ENABLE_EXAMPLE_ROUTINE) {
      autoManager.register(auto.routine("Safe Defense")
          .pathfind("Move to defensive safe position", auto, FieldConstants.DEFENSIVE_SAFE_POSITION)
          .step("Stow mechanisms", auto::stow)
          .build());
      autoManager.register(auto.routine("Example: Time Aware")
          .pathfind("Leave start area", auto, FieldConstants.EXAMPLE_AUTO_EXIT)
          .whenScoringStillAvailable(
              "Score only if the live pose can still reach it safely",
              auto,
              FieldConstants.SCORE_MID,
              AutoConstants.SCORING_ACTION_ESTIMATED_SECONDS,
              AutoConstants.SCORING_TIME_SAFETY_MARGIN_SECONDS,
              () -> auto.sequence(
                  auto.pathfindTo(FieldConstants.SCORE_MID),
                  auto.goal(SuperstructureGoal.PRIMARY_ACTION)),
              auto::stow)
          .build());
    }
  }
}
