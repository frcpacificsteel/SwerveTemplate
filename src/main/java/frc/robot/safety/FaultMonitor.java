package frc.robot.safety;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringArrayPublisher;
import edu.wpi.first.wpilibj.DriverStation;
import frc.robot.telemetry.RobotAlerts;

/** Latches serious mechanism faults until an intentional disabled-mode reset. */
public class FaultMonitor {
  private record Fault(String message, BooleanSupplier active) {}

  private final String name;
  private final Map<String, Fault> faults = new LinkedHashMap<>();
  private final Set<String> latched = new LinkedHashSet<>();
  private final StringArrayPublisher publisher;

  public FaultMonitor(String name) {
    this.name = name;
    publisher = NetworkTableInstance.getDefault()
        .getTable("Faults")
        .getSubTable(name)
        .getStringArrayTopic("active")
        .publish();
  }

  public void register(String key, String message, BooleanSupplier active) {
    faults.put(key, new Fault(message, active));
  }

  public void periodic() {
    faults.forEach((key, fault) -> {
      if (fault.active().getAsBoolean()) {
        latched.add(key);
      }
    });
    String[] messages = latched.stream().map(key -> faults.get(key).message()).toArray(String[]::new);
    publisher.set(messages);
    RobotAlerts.setWarning(
        "fault-" + name,
        name + " FAULT: " + String.join(", ", messages),
        !latched.isEmpty());
  }

  public boolean hasFault() {
    return !latched.isEmpty();
  }

  public void clearLatchedFaults() {
    if (DriverStation.isDisabled()) {
      latched.clear();
    }
  }
}
