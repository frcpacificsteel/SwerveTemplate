package frc.robot.config;

import com.ctre.phoenix6.CANBus;

/**
 * The one source of truth for logical CAN-bus assignment. Empty means the
 * current primary bus; during the Systemcore migration these names can change
 * without touching subsystem code.
 */
public final class CanTopology {
  public record Device(int id, CANBus bus) {}

  public static final String DRIVETRAIN_BUS_NAME = "";
  public static final String MECHANISM_BUS_NAME = "";
  public static final CANBus DRIVETRAIN_BUS = new CANBus(DRIVETRAIN_BUS_NAME, "./logs/example.hoot");
  public static final CANBus MECHANISM_BUS = new CANBus(MECHANISM_BUS_NAME);

  // Add each real mechanism device here, for example:
  // public static final Device INTAKE_MOTOR = new Device(50, MECHANISM_BUS);

  private CanTopology() {}
}
