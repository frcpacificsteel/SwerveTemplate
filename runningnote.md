# Pre-match quick check

1. Power the robot, radio/access point, Driver Station, and separate Limelight camera.
2. Confirm `RobotStatus/ready` is `true` in the NT4 dashboard.
3. Confirm `Auto/Selected` names the intended routine and `Auto/Readiness` is `READY`.
4. Treat `FALLBACK READY` as a warning: recovery is available, but the selected primary auto failed preflight.
5. Verify the driver and operator controllers are connected on the ports in `ControlConstants`.
6. Confirm the Limelight heartbeat and trusted-pose status when the selected auto requires vision.
7. Clear latched faults only while disabled and only after correcting the underlying hardware problem.

Calibration history remains in `src/main/java/frc/robot/OldValues.md`. Current drivetrain calibration belongs in `config/SwerveConstants.java`.
