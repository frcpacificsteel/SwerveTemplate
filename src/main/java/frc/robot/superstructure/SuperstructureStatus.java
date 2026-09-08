package frc.robot.superstructure;

/** Immutable status consumed by autonomous preflight and telemetry. */
public record SuperstructureStatus(
    SuperstructureGoal requestedGoal,
    boolean ready,
    String detail) {}
