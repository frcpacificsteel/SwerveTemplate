package frc.robot.subsystems.vision;

/** Compact health snapshot for status displays and autonomous conditions. */
public record VisionHealth(
    boolean connected,
    boolean targetVisible,
    boolean recentTrustedPose) {}
