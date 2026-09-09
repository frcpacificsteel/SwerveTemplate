# Contributing to Pacific Steel robot code

Robot code is a shared system. A change that looks small on a laptop can affect a driver, a mechanism, or a match. Keep changes easy to review, test them honestly, and leave enough context for the next student to pick up the work.

## Before you start

1. Check for an existing issue or create one that describes the problem or intended behavior.
2. Keep one pull request focused on one outcome. Split unrelated cleanup into a separate change.
3. Read the relevant part of [REPOSITORY_GUIDE.md](REPOSITORY_GUIDE.md) before changing configuration, autonomous behavior, safety, or controls.

For new-season work, use [NEW_SEASON_CHECKLIST.md](NEW_SEASON_CHECKLIST.md). The template contains placeholders by design; do not treat a successful build as hardware validation.

## Making a change

- Use names that explain the robot behavior, not just the implementation.
- Put season-tunable values in `config/`; do not scatter CAN IDs, controller ports, field poses, or measured timings through commands.
- Keep controller hardware in `controls/`. Subsystems should not read an Xbox controller directly.
- Route mechanism requests through `SuperstructureGoal` and the superstructure. Do not bypass safety interlocks to make a mechanism move.
- Add or update the team-facing documentation when controls, dashboard messages, autonomous behavior, or match procedures change.

## Test at the right level

Run the build for every code change:

```powershell
.\gradlew.bat build
```

Run simulation when it applies:

```powershell
.\gradlew.bat simulateJava
```

Then test on hardware when the change can affect it. For drivetrain, controls, CAN configuration, vision, autonomous, safety, or deploy assets, record the conditions you used: robot support state, enabled mode, field setup, selected auto, and what actually happened.

Never test a mechanism in a way that could injure someone or damage the robot. Use the team's pit procedures, keep clear of moving hardware, and ask a mentor or experienced student when you are unsure.

## Opening a pull request

Include:

- The issue it addresses, when there is one
- What changed from the driver's or robot's point of view
- The validation you actually completed, including build, simulation, or hardware evidence
- Any remaining risk, follow-up work, or configuration the reviewer must check

Ask for review from someone who understands the subsystem you changed. Do not self-merge robot-impacting changes unless a mentor has explicitly made an exception.

## Commit messages

Start every commit message with a conventional type and a colon. Use a short, imperative summary after the colon.

```text
feat: add intake homing check
fix: stop auto when vision pose is stale
docs: clarify pre-match readiness check
refactor: isolate drivetrain target selection
test: cover rejected vision estimate
build: update PathPlanner vendor dependency
chore: refresh editor settings
```

Use `feat` for new robot or team behavior, `fix` for a correction, and `docs` for documentation-only work. Do not use vague messages such as `updates`, `changes`, or `final`.

## Before merging

- The branch protection checks pass.
- The reviewer understands the scope and validation.
- The change does not quietly alter unrelated behavior.
- The documentation still describes the robot people will actually use.

After a robot-impacting merge, recheck the behavior on the robot before relying on it at practice or an event.
