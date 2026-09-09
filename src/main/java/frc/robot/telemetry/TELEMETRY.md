# Telemetry and readiness

`RobotStatus` combines health checks into `RobotStatus/ready` and `RobotStatus/unhealthy`; `RobotAlerts` sends Driver Station warnings; and `Tunables` provides bounded values for controlled testing.

When the robot is not ready, read `RobotStatus/unhealthy` first. Fix the named condition, then confirm the warning clears. Add telemetry only when someone at the field knows how to act on it.
