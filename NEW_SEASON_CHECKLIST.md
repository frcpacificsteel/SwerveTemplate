# New Season Checklist

Use this checklist after game reveal and before treating the template as competition-ready.

## Project and hardware

- [ ] Set the team number, deploy target, and robot name.
- [ ] Choose `RobotConfig.ACTIVE_PROFILE` for competition, practice, or simulation.
- [ ] Update controller ports in `ControlConstants`.
- [ ] Record every CAN device in `CanTopology`; verify no duplicate ID/bus pair exists.
- [ ] Recalibrate modules and verify `SwerveConstants` motor IDs, inversions, offsets, gains, and geometry.
- [ ] Replace `TemplateMechanismSubsystem` with real subsystems; add connection, limit, homing, and fault interlocks.
- [ ] Update `SuperstructureGoal` and `Superstructure` for the real mechanisms.

## Field and vision

- [ ] Update field dimensions and every blue-origin pose in `FieldConstants`.
- [ ] Set the defensive recovery pose in `FieldConstants.DEFENSIVE_SAFE_POSITION`.
- [ ] Configure the Limelight name, mounting, pipeline, AprilTags, and pose filters in `VisionConstants` and the Limelight interface.
- [ ] Regenerate the PathPlanner navgrid for the official field if pathfinding will be used.
- [ ] Open both sample `.path` files in PathPlanner; replace them with season-specific paths or delete them.

## Autonomous

- [ ] Create named Java routines in `autos/Autos.java`; do not create `.auto` files.
- [ ] Use `followPath` for intended starts and `pathfindThenFollowPath` for recovery branches.
- [ ] Use `whenScoringStillAvailable` before a score route; measure scoring duration and tune the time margin in `AutoConstants`.
- [ ] Create a `Safe Defense` routine and set a clear defensive recovery pose.
- [ ] Configure each primary routine's fallback order, vision requirements, fault-abort policy, and timeout in `AutoConstants`.
- [ ] Verify every auto can be selected through Elastic and the disabled controller POV.

## Elastic and match readiness

- [ ] Create the Auto, RobotStatus, and Vision widgets described in `REPOSITORY_GUIDE.md`.
- [ ] Confirm `Auto/Readiness = READY`, `Auto/Selected` is correct, and `RobotStatus/ready = true` before autonomous.
- [ ] Verify warnings are understandable with the robot disconnected, a controller unplugged, Limelight unavailable, and a mechanism fault injected safely.
- [ ] Run each intended autonomous routine from its legal start pose, including every recovery branch.

## Release gate

- [ ] Build with the WPILib Java 17 toolchain: `./gradlew build`.
- [ ] Run simulation and hardware checkout.
- [ ] Review the diff, commit the season configuration, and tag the competition baseline.
