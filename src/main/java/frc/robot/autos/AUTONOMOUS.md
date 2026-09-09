# Autonomous routines

`Autos.java` is the normal entry point for creating a routine. `AutoManager` validates selection and publishes readiness; nearby classes provide named steps, runtime state, checks, and recovery policy.

Build routines from Java steps and saved `.path` files. Do not create PathPlanner `.auto` assets. When a routine will not select or run, inspect `Auto/Requested`, `Auto/Selected`, `Auto/Readiness`, `Auto/Executing`, and `Auto/Step` in Elastic while disabled. Fix the stated prerequisite before bypassing it.
