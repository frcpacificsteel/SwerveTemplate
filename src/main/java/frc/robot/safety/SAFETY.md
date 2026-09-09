# Safety and faults

This folder contains the safety boundary for mechanisms: interlocks, homing state, and latched-fault handling. Hard interlocks cannot be bypassed. Soft overrides are limited to Test mode.

When a mechanism will not move, read the published `Safety/.../safe`, blocker, and `Faults/.../active` values first. Fix the real cause, then clear a latched fault while disabled. Never remove a guard or repeatedly clear a fault just to make a warning disappear.
