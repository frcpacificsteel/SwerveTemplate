# Pacific Steel 5025 swerve template

This is FRC Team 5025's 2026 Java starting point for a command-based swerve robot. It is meant to be copied at the beginning of a season, then turned into the team's real robot code with deliberate hardware configuration, tested mechanisms, and field-proven autonomous routines.

The template includes a CTRE Phoenix 6 swerve drivetrain, isolated Limelight vision, Java-defined autonomous routines, NT4 telemetry, and safety scaffolding for future mechanisms. It starts conservatively: the only enabled autonomous routine is `Do Nothing` until the team configures and validates its own routines.

## Before you use it

This repository is not ready to deploy to a competition robot as-is. After game reveal, work through the [new season checklist](NEW_SEASON_CHECKLIST.md). In particular, replace placeholder field data, CAN assignments, swerve calibration, vision configuration, and the hardware-free mechanism skeleton before testing the robot.

The two sample PathPlanner paths are examples, not competition paths. The team keeps path files for authored trajectories, but assembles autonomous routines in Java rather than committing PathPlanner `.auto` files.

## Quick start

Install the 2026 WPILib Development Suite and use its Java 17 toolchain. Then clone the repository and build it.

```powershell
.\gradlew.bat build
```

Useful commands:

```powershell
.\gradlew.bat simulateJava
.\gradlew.bat deploy
```

Run a build before opening a pull request. A successful build checks compilation; it does not prove the code is safe on a real robot.

## Where things live

| Area | Start here |
| --- | --- |
| Season and hardware settings | [`src/main/java/frc/robot/config/`](src/main/java/frc/robot/config/) |
| Controller bindings | [`src/main/java/frc/robot/controls/`](src/main/java/frc/robot/controls/) |
| Autonomous routines | [`src/main/java/frc/robot/autos/Autos.java`](src/main/java/frc/robot/autos/Autos.java) |
| Subsystems, vision, and mechanisms | [`src/main/java/frc/robot/subsystems/`](src/main/java/frc/robot/subsystems/) |
| Cross-mechanism goals | [`src/main/java/frc/robot/superstructure/`](src/main/java/frc/robot/superstructure/) |
| Interlocks and fault handling | [`src/main/java/frc/robot/safety/`](src/main/java/frc/robot/safety/) |
| Editable PathPlanner paths | [`src/main/deploy/pathplanner/paths/`](src/main/deploy/pathplanner/paths/) |

## Autonomous and safety

Autos are registered in Java. A routine can follow a saved path, pathfind from its live pose to recover from a delay, run mechanism actions, and choose a shorter route when there is no longer enough match time to score safely. The built-in recovery pattern stows mechanisms and uses a configured defensive location; if that cannot run safely, it falls back to `Do Nothing`.

Before a match, use Elastic or another NT4 dashboard to confirm:

- `RobotStatus/ready` is `true`
- `Auto/Selected` is the intended routine
- `Auto/Readiness` says `READY`
- active warnings and latched faults have been understood and fixed

`FALLBACK READY` means the robot has a safer alternative available. It does not mean the selected primary routine is ready to run.

## Guides

- [Team guide](TEAM_GUIDE.md) explains controls, dashboard messages, autonomous behavior, and fault recovery for drivers, pit crew, mentors, scouts, and new students.
- [Repository guide](REPOSITORY_GUIDE.md) covers code structure, configuration, autonomous routines, safety, vision, and Elastic in detail.
- [New season checklist](NEW_SEASON_CHECKLIST.md) is the release gate for turning this template into a competition robot.
- [Contributing guide](CONTRIBUTING.md) explains how we make and review changes.

## License

See [WPILib-License.md](WPILib-License.md) for the included WPILib license information.
