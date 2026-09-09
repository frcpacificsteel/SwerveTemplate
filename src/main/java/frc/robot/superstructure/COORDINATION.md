# Superstructure coordination

The superstructure translates a high-level request such as `STOW` or a scoring action into coordinated mechanism behavior. `SuperstructureGoal` names requests, `Superstructure` owns the coordination, and `SuperstructureStatus` reports whether the requested behavior is ready.

If an autonomous action is blocked, inspect the requested goal and status detail before changing the auto. The actual cause may be an unhomed, disconnected, or interlocked mechanism.
