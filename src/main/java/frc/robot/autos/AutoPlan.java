package frc.robot.autos;

import java.util.List;
import java.util.function.Supplier;

import edu.wpi.first.wpilibj2.command.Command;

/** A selectable autonomous routine and the checks that must pass before it can run. */
public record AutoPlan(
    String name,
    Supplier<Command> commandFactory,
    List<AutoCheck> checks,
    List<String> fallbackNames,
    AutoRecoveryPolicy recoveryPolicy) {

  public boolean isReady() {
    return checks.stream().allMatch(AutoCheck::passes);
  }

  public String firstFailure() {
    return checks.stream()
        .map(AutoCheck::failure)
        .flatMap(java.util.Optional::stream)
        .findFirst()
        .orElse("Ready");
  }

  public static AutoPlan of(String name, Supplier<Command> commandFactory, List<AutoCheck> checks) {
    return new AutoPlan(name, commandFactory, List.copyOf(checks), List.of(), AutoRecoveryPolicy.none());
  }

  public AutoPlan withFallbacks(String... names) {
    return new AutoPlan(name, commandFactory, checks, List.of(names), recoveryPolicy);
  }

  public AutoPlan withFallbacks(List<String> names) {
    return new AutoPlan(name, commandFactory, checks, List.copyOf(names), recoveryPolicy);
  }

  public AutoPlan withRecoveryPolicy(AutoRecoveryPolicy policy) {
    return new AutoPlan(name, commandFactory, checks, fallbackNames, policy);
  }

  public AutoPlan withCheck(AutoCheck check) {
    var updatedChecks = new java.util.ArrayList<>(checks);
    updatedChecks.add(check);
    return new AutoPlan(name, commandFactory, List.copyOf(updatedChecks), fallbackNames, recoveryPolicy);
  }
}
