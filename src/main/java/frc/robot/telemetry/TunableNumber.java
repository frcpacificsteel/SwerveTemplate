package frc.robot.telemetry;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.networktables.DoubleEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import frc.robot.config.RobotConfig;

/** A bounded NT4 number that falls back to a reviewed code constant when tuning is disabled. */
public class TunableNumber {
  private final double defaultValue;
  private final double minimum;
  private final double maximum;
  private final DoubleEntry entry;

  public TunableNumber(String name, double defaultValue, double minimum, double maximum) {
    this.defaultValue = defaultValue;
    this.minimum = minimum;
    this.maximum = maximum;
    entry = NetworkTableInstance.getDefault()
        .getTable("Tunables")
        .getDoubleTopic(name)
        .getEntry(defaultValue);
    entry.setDefault(defaultValue);
  }

  public double get() {
    double value = RobotConfig.TUNING_ENABLED ? entry.get() : defaultValue;
    return Double.isFinite(value) ? MathUtil.clamp(value, minimum, maximum) : defaultValue;
  }
}
