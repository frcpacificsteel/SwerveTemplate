package frc.robot.autos;

import java.util.function.BooleanSupplier;

/** Conditions that may interrupt a running auto and send execution to a fallback. */
public record AutoRecoveryPolicy(
    BooleanSupplier continueCondition,
    String failureReason,
    double timeoutSeconds) {

  public static AutoRecoveryPolicy none() {
    return new AutoRecoveryPolicy(() -> true, "", 0.0);
  }

  public boolean hasGuard() {
    return timeoutSeconds > 0.0 || !failureReason.isBlank();
  }
}
