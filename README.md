# FRC Team 5025 - Pacific Steel Swerve Template

This repository is Team 5025's 2026 WPILib Java template. It gives a new robot project a safe, understandable starting point for swerve, separate Limelight vision, mechanisms, autonomous, health checks, and field-side debugging.

## Overview

The code uses WPILib's command-based model with isolated configuration, intent-based controls, Java-defined autonomous routines, safety, a goal-based superstructure, Limelight vision, and dashboard-neutral NT4 telemetry.

### Key Components

- Drivetrain & Kinematics: Swerve drive control integrated with CTRE Phoenix 6 and PathPlanner libraries.
- Subsystems & Mechanisms: Modular components representing physical robot hardware and actuators.
- Autonomous: small Java steps, condition branches, actual-time route selection, explicit selection warnings, and safe recovery.
- Safety & Superstructure: reusable interlocks, latched faults, and goal-based mechanism coordination.
- Vision & Telemetry: separate Limelight integration and dashboard-neutral NT4 status.

## Prerequisites & Tools

- Java Development Kit 17 for the 2026 WPILib toolchain
- WPILib Development Suite
- Gradle build tool (included wrapper `./gradlew`)
- Driver Station plus an NT4-compatible telemetry viewer (for example, Elastic or AdvantageScope)

## Build and Deployment Commands

To build the robot project:

```bash
./gradlew build
```

To deploy code to the roboRIO:

```bash
./gradlew deploy
```

To run in simulation mode:

```bash
./gradlew simulateJava
```

## First setup for a new robot

1. Set the active hardware profile in `config/RobotConfig.java`.
2. Set controller ports in `config/ControlConstants.java`.
3. Add every CAN device to `config/CanTopology.java`; drivetrain calibration stays in `config/SwerveConstants.java`.
4. Replace the placeholder poses in `config/FieldConstants.java` after game reveal.
5. Turn `TemplateMechanismSubsystem` into each real mechanism, then wire its goals through `Superstructure`.
6. Create match routines in `autos/Autos.java`. The editable PathPlanner `.path` library is retained; `.auto` files are intentionally not used.
7. Build with Java 17: `./gradlew build`, then verify `RobotStatus/ready` and the selected auto before driving.

## Autonomous in one minute

Autos are Java code. This means a programmer can use normal names, methods, conditions, and code review—without manually keeping a dashboard chooser, event names, and `.auto` assets synchronized. Use `.path` files for authored trajectories, but assemble them in Java.

```java
autoManager.register(auto.routine("Leave And Decide")
    .followPath("Leave start", auto, "Example Mobility")
    .whenScoringStillAvailable(
        "Score only if the live pose can reach it safely",
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

The score branch is decided when reached, from the robot's live pose and remaining autonomous time. It estimates travel time from that position, adds the configured scoring duration and safety margin, then skips the score if it is no longer feasible. `Do Nothing` is the only enabled routine until your team adds its own.

For saved paths, use `.followPath("Leave Start")`. To recover from an interrupted or delayed route, use `.pathfindThenFollowPath("Score Route")`; it plans from the robot's current pose, then joins and follows the saved path.

For full game-reveal preparation and Elastic dashboard setup, use [NEW_SEASON_CHECKLIST.md](NEW_SEASON_CHECKLIST.md).

## For drivers, mentors, and new students

Read [TEAM_GUIDE.md](TEAM_GUIDE.md) for a non-programmer explanation of how the robot code, Elastic dashboard, safety behavior, and autonomous recovery work.

## Repository Guide

For detailed setup instructions, hardware mapping, controller configuration, autonomous selection, and guides on modifying code for new mechanisms, refer to `REPOSITORY_GUIDE.md` located in the root directory.
