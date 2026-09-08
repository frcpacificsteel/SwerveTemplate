package frc.robot.telemetry;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringArrayPublisher;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * Centralized robot warnings. Keeping Driver Station reporting here limits the
 * 2027 migration surface and also gives dashboards a stable NT4 status feed.
 */
public final class RobotAlerts {
  private static final Map<String, String> ACTIVE_WARNINGS = new LinkedHashMap<>();
  private static final StringArrayPublisher WARNING_PUBLISHER = NetworkTableInstance
      .getDefault()
      .getTable("RobotStatus")
      .getStringArrayTopic("warnings")
      .publish();

  private RobotAlerts() {}

  public static synchronized void setWarning(String key, String message, boolean active) {
    boolean wasActive = ACTIVE_WARNINGS.containsKey(key);

    if (active) {
      ACTIVE_WARNINGS.put(key, message);
      if (!wasActive) {
        DriverStation.reportWarning(message, false);
      }
    } else {
      ACTIVE_WARNINGS.remove(key);
    }

    WARNING_PUBLISHER.set(ACTIVE_WARNINGS.values().toArray(String[]::new));
  }
}
