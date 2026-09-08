package frc.robot.telemetry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringArrayPublisher;

/** Registers robot-health checks in one place and exposes them through NT4 and DS warnings. */
public class RobotStatus {
  private record Check(String message, BooleanSupplier healthy) {}

  private final Map<String, Check> checks = new LinkedHashMap<>();
  private final StringArrayPublisher unhealthyPublisher = NetworkTableInstance.getDefault()
      .getTable("RobotStatus")
      .getStringArrayTopic("unhealthy")
      .publish();
  private final BooleanPublisher readyPublisher = NetworkTableInstance.getDefault()
      .getTable("RobotStatus")
      .getBooleanTopic("ready")
      .publish();

  public void register(String key, String message, BooleanSupplier healthy) {
    checks.put(key, new Check(message, healthy));
  }

  public void periodic() {
    Map<String, Boolean> results = new LinkedHashMap<>();
    checks.forEach((key, check) -> results.put(key, check.healthy().getAsBoolean()));
    String[] unhealthy = results.entrySet().stream()
        .filter(entry -> !entry.getValue())
        .map(entry -> entry.getKey() + ": " + checks.get(entry.getKey()).message())
        .toArray(String[]::new);

    for (Map.Entry<String, Check> entry : checks.entrySet()) {
      RobotAlerts.setWarning(
          "health-" + entry.getKey(),
          entry.getValue().message(),
          !results.get(entry.getKey()));
    }
    unhealthyPublisher.set(unhealthy);
    readyPublisher.set(unhealthy.length == 0);
  }
}
