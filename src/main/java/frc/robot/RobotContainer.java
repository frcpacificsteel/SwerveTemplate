// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.pathplanner.lib.commands.FollowPathCommand;
import com.pathplanner.lib.commands.PathfindingCommand;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.autos.AutoManager;
import frc.robot.autos.AutoFactory;
import frc.robot.autos.Autos;
import frc.robot.commands.drivetrain.FieldTargetManager;
import frc.robot.config.RobotConfig;
import frc.robot.config.SwerveConstants;
import frc.robot.controls.ControlBindings;
import frc.robot.controls.DriverControls;
import frc.robot.controls.OperatorControls;
import frc.robot.subsystems.drivetrain.CommandSwerveDrivetrain;
import frc.robot.subsystems.vision.LimelightVisionSubsystem;
import frc.robot.subsystems.mechanisms.TemplateMechanismSubsystem;
import frc.robot.superstructure.Superstructure;
import frc.robot.telemetry.Telemetry;
import frc.robot.telemetry.RobotStatus;

public class RobotContainer {
    private final Telemetry logger = new Telemetry();
    private final RobotStatus robotStatus = new RobotStatus();
    private final DriverControls driverControls = new DriverControls();
    private final OperatorControls operatorControls = new OperatorControls();

    public final CommandSwerveDrivetrain drivetrain = SwerveConstants.createDrivetrain();

    private final LimelightVisionSubsystem vision = new LimelightVisionSubsystem(drivetrain);
    private final TemplateMechanismSubsystem templateMechanism = new TemplateMechanismSubsystem();
    private final Superstructure superstructure = new Superstructure(templateMechanism);
    private final FieldTargetManager fieldTargets = new FieldTargetManager(drivetrain);
    private final AutoManager autoManager = new AutoManager(
        vision::hasRecentTrustedPose,
        superstructure::isReadyForAuto,
        superstructure::safeStopCommand);
    private final AutoFactory autoFactory = new AutoFactory(fieldTargets, superstructure);
    
    public RobotContainer() {
        Autos.registerAll(autoManager, autoFactory);
        configureBindings();
        robotStatus.register(
            "limelight",
            "LIMELIGHT NOT CONNECTED",
            () -> !RobotConfig.ACTIVE_PROFILE.limelightExpected() || vision.isConnected());
        robotStatus.register("driver-controller", "DRIVER CONTROLLER NOT CONNECTED", driverControls::isConnected);
        robotStatus.register(
            "operator-controller",
            "OPERATOR CONTROLLER NOT CONNECTED",
            () -> !RobotConfig.ACTIVE_PROFILE.operatorControllerExpected() || operatorControls.isConnected());
        robotStatus.register("gyro", "DRIVETRAIN GYRO NOT CONNECTED", () -> drivetrain.getPigeon2().isConnected());
        robotStatus.register("drivetrain", "DRIVETRAIN ODOMETRY NOT HEALTHY", drivetrain::isOdometryHealthy);
        robotStatus.register("template-mechanism", "TEMPLATE MECHANISM NOT READY", templateMechanism::isReady);
        robotStatus.register("auto", "NO VALID AUTONOMOUS ROUTINE SELECTED", autoManager::hasReadySelection);

        // Warmup PathPlanner to avoid Java pauses
        CommandScheduler.getInstance().schedule(
            FollowPathCommand.warmupCommand(),
            PathfindingCommand.warmupCommand());
    }
    private void configureBindings() {
        ControlBindings.configure(
            driverControls,
            operatorControls,
            drivetrain,
            fieldTargets,
            superstructure,
            autoManager);
        drivetrain.registerTelemetry(logger::telemeterize);
    }

    public Command getAutonomousCommand() {
        return autoManager.getSelectedCommand();
    }

    /** Called from Robot.robotPeriodic so disabled-mode auto warnings stay current. */
    public void periodic() {
        autoManager.periodic();
        robotStatus.periodic();
    }
}
