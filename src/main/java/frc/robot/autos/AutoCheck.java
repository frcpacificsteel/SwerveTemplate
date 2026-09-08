package frc.robot.autos;

import java.util.Optional;
import java.util.function.BooleanSupplier;

/** A named condition that must pass before an autonomous routine is match-ready. */
public record AutoCheck(BooleanSupplier condition, String failureMessage) {
  public boolean passes() {
    return condition.getAsBoolean();
  }

  public Optional<String> failure() {
    return passes() ? Optional.empty() : Optional.of(failureMessage);
  }

  public static AutoCheck require(BooleanSupplier condition, String failureMessage) {
    return new AutoCheck(condition, failureMessage);
  }
}
