# Drivetrain subsystem

`CommandSwerveDrivetrain` owns CTRE swerve hardware, odometry, and drivetrain-facing commands. Swerve IDs, offsets, gains, and geometry stay in `config/SwerveConstants.java`.

For a module that behaves incorrectly, verify CAN IDs, inversion, absolute encoder offset, and physical wiring before changing gains. For bad field-relative driving, check gyro orientation, pose reset, and alliance state. Support the robot safely before module tests.
