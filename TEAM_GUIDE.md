# Team Guide: How This Robot Code Works

This guide is for drivers, operators, mentors, pit crew, scouts, and new students. You do not need to know Java to use it. Its job is to explain what the robot software is trying to do, what the team should check before a match, and when to get a programmer involved.

## The big picture

Think of the code as a set of small specialists that work together:

- The **drivetrain** moves and turns the robot.
- The **controllers** turn driver and operator button presses into requests.
- The **superstructure** turns a request such as “score” or “stow” into safe mechanism behavior.
- The **Limelight** camera helps the robot estimate where it is on the field.
- The **autonomous manager** chooses and monitors the opening routine.
- The **safety system** blocks unsafe mechanism actions and reports faults.
- **Elastic** is the team’s live information board.

The robot is designed to prefer a safe stop or simpler plan over continuing an auto that is no longer trustworthy.

## What happens when the robot turns on

1. The robot checks whether important hardware is present: controllers, gyro, drivetrain, Limelight when expected, and mechanisms.
2. It publishes those checks to Elastic as `RobotStatus`.
3. It loads the Java autos that the programmers registered. It does **not** load `.auto` files.
4. Until someone selects a valid autonomous routine, the robot intentionally warns that no auto is ready.
5. During teleop, the driver controls the drivetrain and the operator requests mechanism goals.

## Controls in plain language

The exact control mapping can change as the season develops. The current baseline is:

| Who | Control | Result |
|---|---|---|
| Driver | Left stick | Drive across the field |
| Driver | Right-stick X | Turn |
| Driver | X | Hold swerve brake |
| Driver | Back | Reset the field-relative driving direction |
| Driver | Y | Pathfind to the currently selected field target |
| Driver | POV Left / Right / Down | Choose a field target |
| Driver | B | Request the primary robot action |
| Driver, disabled only | POV Up / Down | Cycle through autonomous routines |
| Operator | A | Request the primary robot action |
| Operator | B | Stow mechanisms |
| Operator | Start, held | Request a manual safety override; only works in Test mode and never bypasses hard safety limits |
| Operator, disabled only | Back | Clear a latched fault after the underlying issue is fixed |

If the robot does not react as expected, do not repeatedly press buttons. Check Elastic warnings first, then ask the pit/programming team to diagnose the cause.

## Elastic: the match information board

Elastic should have three panels.

### 1. Match-ready panel

These are the most important values before a match:

- `RobotStatus/ready` must be `true`.
- `RobotStatus/unhealthy` must be empty.
- `RobotStatus/warnings` should not contain an active problem.
- `Auto/Selected` must show the intended routine.
- `Auto/Readiness` must say `READY`.

If any of these are wrong, treat the robot as not match-ready until the responsible team member resolves it.

### 2. Autonomous panel

- `Auto/Available`: routines the programmers registered.
- `Auto/Requested`: a place to type the exact routine name while disabled.
- `Auto/Selected`: the chosen main routine.
- `Auto/Readiness`: whether the selected main routine can run safely.
- `Auto/Executing`: what is actually running, including a fallback.
- `Auto/Step`: the current part of the routine.

`FALLBACK READY` is not a green light for the intended routine. It means the selected routine has a problem, but the robot has a safer alternative available.

### 3. Vision and safety panel

- `Vision/targetVisible`: the camera currently sees a target.
- `Vision/trustedPose`: the robot is accepting a recent camera position estimate.
- `Vision/rejectionReason`: why the latest camera estimate was ignored, if one was ignored.
- `Safety/.../safe`: the mechanism is allowed to move.
- `Faults/.../active`: problems that remain latched until cleared while disabled.

## How autonomous works

Autos are written in Java by the programming team, made from simple named steps. They may drive to a position, follow a saved route, run a mechanism action, or choose between alternatives.

The team keeps editable `.path` files for authored routes. The code combines those routes; it does not use PathPlanner `.auto` files.

There are two common driving choices:

- **Follow path:** start at the path’s expected beginning and follow the route exactly.
- **Recover, then follow path:** if the robot is delayed or out of position, first pathfind from its current position, then join and follow the saved route.

Before starting a score route, the code can ask: “From where the robot is right now, is there enough auto time left to drive there, perform the score, and still keep a safety margin?” If the answer is no, it takes a shorter route or stows instead. This calculation is a conservative estimate, so it must be tuned using real on-field timing.

If a mechanism faults during an auto, the intended recovery pattern is to stow and move to a configured defensive location. If that is not safe or available, the robot falls back to `Do Nothing` rather than guessing.

## What the safety features mean

Safety is not there to make the robot slower; it is there to stop one bad sensor, disconnected motor, or failed mechanism from becoming a larger problem.

- A **hard interlock** cannot be bypassed. Example: a mechanism should not move if its controller is disconnected.
- A **soft interlock** may be bypassed only in Test mode, for controlled diagnostics.
- A **latched fault** remains visible until the team fixes the cause and clears it while disabled.
- A **safe stop** stows the superstructure when autonomous recovery runs out of safe options.

Never clear a fault just to remove a red warning. Find the cause first.

## What is intentionally unfinished in a fresh template

This repository is a starting point, not a finished competition robot. In particular:

- `TemplateMechanismSubsystem` has no physical hardware yet. Programmers replace it with real subsystems as mechanisms are designed.
- The two sample paths are examples only. They must be replaced or deleted for the actual field.
- Field poses, Limelight configuration, CAN assignments, mechanism limits, scoring timings, and defensive position are placeholders until the season is known.
- `Do Nothing` is the only default enabled auto. The example auto is intentionally disabled until it has been configured and tested.

## Who updates what

| Team role | Typical responsibility |
|---|---|
| Drivers and operators | Report control issues, review Elastic before matches, and help measure auto timing |
| Pit crew | Verify wiring, controller connection, mechanism limits, and reported faults |
| Programmers | Update code, auto routines, field poses, dashboard names, and safety checks |
| Drive coach | Confirm selected auto, field-side plan, and fallback strategy before each match |
| Mentors | Review safety, changes, and the season checklist before competition |

## Keep this guide alive

**This guide must be updated as the season progresses.** Every time the team changes controls, adds a mechanism, creates an auto, changes the Elastic layout, discovers a failure mode, or changes match procedures, update this guide and the related technical documentation in the same change.

At minimum, review it after game reveal, after the first full robot test, before each event, and after any issue that surprises the drive team. A guide that describes an old robot is worse than no guide at all.
