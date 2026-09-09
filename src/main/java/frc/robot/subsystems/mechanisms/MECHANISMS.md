# Mechanism subsystems

This folder is for subsystems that own real scoring, intake, climbing, or other mechanism hardware. `TemplateMechanismSubsystem` intentionally owns no CAN devices and is safe to leave in place before a real mechanism exists.

When adding a mechanism, create its constants, record every device in `config/CanTopology`, keep motor and sensor setup inside the subsystem, and publish health information to diagnose it. Gate every output through connection, limit, homing, and fault checks. If it will not move, inspect those checks before changing gains or bypassing a safeguard.
