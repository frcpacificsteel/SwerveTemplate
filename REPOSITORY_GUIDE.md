# FRC Team 5025 — Repository Guide

This is a 2026 WPILib command-based template designed to keep game-specific work easy to replace and the eventual 2027 Systemcore migration contained.

## Project Structure

All robot code is under `src/main/java/frc/robot/`:

- `RobotContainer.java`: constructs and connects the robot's major components.
- `config/`: frequently adjusted values and season policy.
- `controls/`: controller hardware and all button bindings.
- `autos/`: Java routine builder, selection, conditions, fallbacks, time-aware branches, and recovery.
- `commands/`: reusable robot actions such as field-target pathfinding.
- `subsystems/`: drivetrain, Limelight vision, and mechanism hardware.
- `superstructure/`: high-level goals spanning mechanisms.
- `safety/`: interlocks, homing state, and latched faults.
- `telemetry/`: NT4 data, health status, warnings, and bounded tunables.
- `util/`: alliance and field-coordinate helpers.
- `config/SwerveConstants.java`: CTRE drivetrain calibration and drivetrain factory.

## Fast Iteration

Start in `config/` for normal pit and practice changes:

- `DriveConstants.java`: drive scale, deadband, and path constraints.
- `VisionConstants.java`: Limelight name, heartbeat timeout, and pose filters.
- `FieldConstants.java`: field dimensions and blue-origin target poses.
- `CanTopology.java`: logical bus names and non-swerve CAN IDs.
- `ControlConstants.java`: driver and operator USB ports.
- `TemplateMechanismSubsystem.java`: hardware-free, documented mechanism skeleton.
- `AutoConstants.java`: match duration, fallback chains, requirements, timeouts, and example-routine switch.
- `RobotConfig.java`: active robot profile and runtime-tuning permission.

Keep swerve IDs, offsets, and gains together in `config/SwerveConstants.java`. Re-export from Tuner X after recalibration if that is your workflow, then preserve any team-maintained edits intentionally.

When `RobotConfig.TUNING_ENABLED` is `false`, reviewed code constants always win. When enabled for controlled testing, bounded values under `Tunables/` may override the supported tuning points.

## Controls

`DriverControls` and `OperatorControls` translate controller hardware into named intent. `ControlBindings` connects that intent to commands. Subsystems must never read an Xbox controller directly.

Current driver controls:

- Left stick: field-centric translation.
- Right stick X: rotation.
- X: swerve brake.
- Back: reset field heading.
- Y: toggle pathfinding to the selected field target.
- POV Left/Right/Down: select a field target.
- B: request the primary superstructure action.
- While disabled, POV Up/Down: cycle autonomous selection.

Current operator controls:

- A: request the primary superstructure action.
- B: stow.
- Start: hold manual safety override. It only becomes active in Test mode and cannot bypass hard interlocks.
- While disabled, Back: clear latched faults after the underlying problem is corrected.

## Limelight Vision

`LimelightVisionSubsystem` is the only class that calls `LimelightHelpers`. It supplies robot orientation for MegaTag 2, tracks the Limelight heartbeat, filters estimates, and gives the drivetrain only trusted measurements.

Published NT4 fields include:

- `Vision/targetVisible`
- `Vision/trustedPose`
- `Vision/tagCount`
- `Vision/rejectionReason`

Camera mounting and AprilTag configuration remain in the Limelight interface. Change the camera's NetworkTables name in `VisionConstants.LIMELIGHT_NAME`.

## Autonomous Selection and Recovery

Autos are Java-defined; there are no `.auto` files. The editable `.path` library and its PathPlanner navgrid remain available for route authoring. This removes stale, opaque auto assemblies, unnamed event commands, and hidden dashboard chooser state while retaining the useful authored trajectories. PathPlanner powers both saved-path following and runtime recovery pathfinding.

The template deliberately starts with no selected auto. A valid choice is required before `RobotStatus/ready` can become true. `Autos.registerAll(...)` registers `Do Nothing` and, only when explicitly enabled, a safe-to-copy example.

`AutoManager` publishes:

- `Auto/Available`: Java routines registered by `Autos.java`.
- `Auto/Requested`: dashboard-requested routine name.
- `Auto/Selected`: requested primary routine.
- `Auto/Readiness`: `READY`, a reason the selected auto is blocked, or `FALLBACK READY: <name>`.
- `Auto/Executing`: the primary, fallback, or safe-stop routine actually running.
- `Auto/Step`: the step currently being executed.

Select a routine by writing its exact name to `Auto/Requested` from an NT4 dashboard, or use driver POV Up/Down while disabled. An invalid request leaves the current selection unchanged and posts a warning.

### Creating a routine

Open `autos/Autos.java`. Each routine is a sequence of named Java steps. The normal building blocks are:

- `pathfind(stepName, auto, blueOriginPose)` — drives to a blue-origin pose and automatically mirrors it for red.
- `followPath(stepName, auto, pathName)` — follows `src/main/deploy/pathplanner/paths/<pathName>.path`.
- `pathfindThenFollowPath(stepName, auto, pathName)` — recovers from the current robot pose, then joins and follows the saved path.
- `step(stepName, commandSupplier)` — runs any normal WPILib command.
- `auto.sequence(...)` / `auto.parallel(...)` — combines route and mechanism commands without extra imports.
- `waitSeconds(stepName, auto, seconds)` — explicit timing pause.
- `when(stepName, condition, trueRoute, falseRoute)` — chooses from live robot state when reached.
- `whenTimeRemaining(stepName, seconds, fullRoute, shortRoute)` — chooses from real elapsed autonomous time when reached.
- `whenScoringStillAvailable(...)` — estimates travel from the live robot pose, then chooses scoring or an alternate route.
- `require(condition, message)` — blocks selection until a routine-specific prerequisite is true.

`whenTimeRemaining` is the intended answer to a routine that takes longer than planned. It does not trust an estimated path duration: it checks the actual elapsed match time at the decision point. Measure a full route on the robot, add margin, and set `seconds` to the minimum time needed to finish safely. If the robot is late, its short route runs instead.

`whenScoringStillAvailable` is the standard scoring rule. It estimates the pathfinding travel time from the robot's current pose to the selected scoring pose, adds `SCORING_ACTION_ESTIMATED_SECONDS` and `SCORING_TIME_SAFETY_MARGIN_SECONDS`, then compares that total with the real remaining autonomous time. This gives every field position its own feasibility decision. It is conservative planning guidance, not a promise: tune the action time and margin from field measurements.

Use `followPath` when the robot should begin at the path's authored starting pose. Use `pathfindThenFollowPath` for a recovery branch, for example after a missed game piece or a slow score. It starts from the robot's current estimate and joins the next saved route instead of assuming the original path start is still valid. Both methods preflight the file name, so a missing path produces an `Auto/Readiness` warning before a match.

```java
autoManager.register(auto.routine("Leave And Decide")
    .followPath("Leave start", auto, "Example Mobility")
    .whenScoringStillAvailable(
        "Score if the live pose can reach it safely",
        auto,
        FieldConstants.SCORE_MID,
        AutoConstants.SCORING_ACTION_ESTIMATED_SECONDS,
        AutoConstants.SCORING_TIME_SAFETY_MARGIN_SECONDS,
        () -> auto.sequence(
            auto.pathfindThenFollowPath("Example Recovery Route"),
            auto.goal(SuperstructureGoal.PRIMARY_ACTION)),
        auto::stow)
    .build());
```

Keep every named pose in `FieldConstants`, every speed limit in `DriveConstants`, and every safety/fallback policy in `AutoConstants`. This lets programmers tune a route without digging through command logic.

### Global recovery policy

Configure policies in `AutoConstants`:

```java
public static final Map<String, List<String>> FALLBACKS = Map.of(
    "Leave And Decide", List.of("Safe Defense", "Do Nothing"));

public static final Set<String> REQUIRES_TRUSTED_VISION = Set.of("Primary Auto");
public static final Set<String> ABORTS_ON_SUPERSTRUCTURE_FAULT = Set.of("Leave And Decide");
public static final Map<String, Double> TIMEOUT_SECONDS = Map.of("Leave And Decide", 12.0);
```

Every routine except `Do Nothing` has a 14.5-second default safety timeout. Configure a shorter per-routine timeout when appropriate. The included `Safe Defense` pattern pathfinds to `FieldConstants.DEFENSIVE_SAFE_POSITION` and stows mechanisms. Configure it as the first fallback for a mechanism-faulting primary routine; if it is unavailable, the manager uses `Do Nothing` and stows.

Register fallback routines before the primary routine that names them. A new routine automatically falls back to `Do Nothing` unless it has an explicit `FALLBACKS` entry.

## Superstructure and Mechanisms

Controls and autos request `SuperstructureGoal` values instead of manipulating motors. The superstructure converts a goal into mechanism behavior and reports whether it is ready for autonomous use.

`TemplateMechanismSubsystem` is intentionally hardware-free. For a new mechanism:

1. Create a mechanism-specific constants class in `config/`.
2. Add each CAN device to `CanTopology`.
3. Construct and configure hardware inside the subsystem.
4. Add connection, homing, limit, and fault conditions.
5. Gate outputs with `SafetyManager.isSafe()`.
6. Add goals to `SuperstructureGoal` and coordinate them in `Superstructure`.
7. Add a routine-specific action or branch in `Autos.java` when the mechanism is needed in autonomous.

## Safety and Readiness

`SafetyManager` supports:

- Hard interlocks that can never be overridden.
- Soft interlocks that can only be overridden while Test mode is enabled.
- NT4 `safe`, `blockers`, and `manualOverrideActive` status.
- Driver Station warnings for blocked mechanisms and active overrides.

`FaultMonitor` latches serious failures until an intentional disabled-mode clear. `HomingState` provides consistent `UNHOMED`, `HOMING`, `READY`, and `FAULT` states for future mechanisms.

`RobotStatus` currently checks:

- selected autonomous readiness,
- driver/operator controller connection,
- Limelight heartbeat when expected by the active profile,
- drivetrain gyro and odometry,
- template-mechanism readiness.

Before a match, require `RobotStatus/ready = true`, confirm `Auto/Selected`, and require `Auto/Readiness = READY`. `FALLBACK READY` means the robot can recover safely, but the selected primary auto is invalid and still needs attention.

## Build and Deployment

Use the WPILib 2026 JDK and tooling:

```powershell
.\gradlew.bat build
.\gradlew.bat simulateJava
.\gradlew.bat deploy
```

The 2027 migration is intentionally deferred. Use the official importer when the team adopts the stable 2027 toolchain; then update Java packages, GradleRIO deployment, vendor libraries, Limelight helper compatibility, and Systemcore CAN bus identifiers.

## Elastic Dashboard

Use Elastic as the match dashboard. Build an Auto panel from these NT4 values:

- `Auto/Available`: selectable Java routine names.
- `Auto/Requested`: editable string; write an exact name while disabled.
- `Auto/Selected`, `Auto/Readiness`, `Auto/Executing`, and `Auto/Step`: prominent text widgets.
- `RobotStatus/ready`, `RobotStatus/unhealthy`, and `RobotStatus/warnings`: prominent readiness and warning widgets.
- `Vision/targetVisible`, `Vision/trustedPose`, `Vision/tagCount`, and `Vision/rejectionReason`: vision-health widgets.

Treat `Auto/Readiness = READY` and `RobotStatus/ready = true` as the match-start gate. Do not rely on a displayed routine name alone.
