package frc.robot.controls;

import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.RotationsPerSecond;

import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest;

import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;
import frc.robot.autos.AutoManager;
import frc.robot.commands.drivetrain.FieldTargetManager;
import frc.robot.config.DriveConstants;
import frc.robot.config.FieldConstants;
import frc.robot.config.SwerveConstants;
import frc.robot.subsystems.drivetrain.CommandSwerveDrivetrain;
import frc.robot.superstructure.Superstructure;
import frc.robot.superstructure.SuperstructureGoal;

/** All control bindings are defined here so RobotContainer only owns composition. */
public final class ControlBindings {
  private ControlBindings() {}

  public static void configure(
      DriverControls driver,
      OperatorControls operator,
      CommandSwerveDrivetrain drivetrain,
      FieldTargetManager fieldTargets,
      Superstructure superstructure,
      AutoManager autoManager) {
    double maxSpeed = DriveConstants.TELEOP_SPEED_SCALE
        * SwerveConstants.kSpeedAt12Volts.in(MetersPerSecond);
    double maxAngularRate = RotationsPerSecond.of(DriveConstants.MAX_ANGULAR_RATE_RPS)
        .in(RadiansPerSecond);

    SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric()
        .withDeadband(maxSpeed * DriveConstants.DRIVE_DEADBAND_PERCENT)
        .withRotationalDeadband(maxAngularRate * DriveConstants.DRIVE_DEADBAND_PERCENT)
        .withDriveRequestType(DriveRequestType.OpenLoopVoltage);
    SwerveRequest.SwerveDriveBrake brake = new SwerveRequest.SwerveDriveBrake();

    drivetrain.setDefaultCommand(drivetrain.applyRequest(() -> {
      double enabledScale = DriverStation.isTeleopEnabled() ? 1.0 : 0.0;
      return drive
          .withVelocityX(driver.driveForward() * maxSpeed * enabledScale)
          .withVelocityY(driver.driveLeft() * maxSpeed * enabledScale)
          .withRotationalRate(driver.rotate() * maxAngularRate * enabledScale);
    }));

    SwerveRequest.Idle idle = new SwerveRequest.Idle();
    RobotModeTriggers.disabled().whileTrue(
        drivetrain.applyRequest(() -> idle).ignoringDisable(true));

    driver.brake().whileTrue(drivetrain.applyRequest(() -> brake));
    driver.resetFieldHeading().onTrue(drivetrain.runOnce(drivetrain::seedFieldCentric));
    driver.pathfindToTarget().toggleOnTrue(fieldTargets.pathfindToSelectedTarget());
    driver.selectLeftTarget().onTrue(fieldTargets.selectTarget(FieldConstants.SCORE_LEFT));
    driver.selectRightTarget().onTrue(fieldTargets.selectTarget(FieldConstants.SCORE_RIGHT));
    driver.selectMiddleTarget().onTrue(fieldTargets.selectTarget(FieldConstants.SCORE_MID));
    driver.runPrimaryGoal().onTrue(superstructure.requestGoal(SuperstructureGoal.PRIMARY_ACTION));

    operator.primaryAction().onTrue(superstructure.requestGoal(SuperstructureGoal.PRIMARY_ACTION));
    operator.stow().onTrue(superstructure.requestGoal(SuperstructureGoal.STOW));
    operator.manualOverride().onTrue(Commands.runOnce(() -> superstructure.requestManualOverride(true)));
    operator.manualOverride().onFalse(Commands.runOnce(() -> superstructure.requestManualOverride(false)));
    RobotModeTriggers.disabled().and(operator.clearFaults())
        .onTrue(superstructure.clearFaultsCommand());

    RobotModeTriggers.disabled().and(driver.selectNextAuto())
        .onTrue(Commands.runOnce(autoManager::selectNext).ignoringDisable(true));
    RobotModeTriggers.disabled().and(driver.selectPreviousAuto())
        .onTrue(Commands.runOnce(autoManager::selectPrevious).ignoringDisable(true));
  }
}
