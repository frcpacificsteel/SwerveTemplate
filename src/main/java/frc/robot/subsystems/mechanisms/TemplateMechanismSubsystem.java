package frc.robot.subsystems.mechanisms;

import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.safety.FaultMonitor;
import frc.robot.safety.HomingState;
import frc.robot.safety.SafetyManager;

/**
 * Hardware-free starting point for one real mechanism. This class intentionally
 * owns no CAN device, so it is safe in a fresh project until a team adds hardware.
 *
 * <p>To turn this into a real subsystem: add a mechanism constants class, construct
 * the motor/sensors here, register connection/limit/homing checks with {@code safety},
 * register serious faults with {@code faults}, and gate every output with {@link #isReady()}.
 */
public class TemplateMechanismSubsystem extends SubsystemBase {
  private static final BooleanPublisher REQUESTED_ACTIVE = NetworkTableInstance.getDefault()
      .getTable("Mechanisms")
      .getBooleanTopic("TemplateMechanismRequestedActive")
      .publish();
  private final SafetyManager safety = new SafetyManager("TemplateMechanism");
  private final FaultMonitor faults = new FaultMonitor("TemplateMechanism");
  private final HomingState homingState = HomingState.NOT_REQUIRED;
  private boolean requestedActive;

  public TemplateMechanismSubsystem() {
    // Add hard interlocks here. Example:
    // safety.addHardInterlock("motor", "Motor controller is not connected", motor::isConnected);
    // Add latched faults here. Example:
    // faults.register("temperature", "Motor is too hot", () -> motorTempCelsius() > MAX_TEMP_C);
  }

  @Override
  public void periodic() {
    faults.periodic();
    safety.periodic();
    REQUESTED_ACTIVE.set(requestedActive);

    // Set real hardware output here, always gated by isReady(). Example:
    // motor.set(requestedActive && isReady() ? output : 0.0);
  }

  public HomingState getHomingState() {
    return homingState;
  }

  /** Replace this with actual connection and homing checks when hardware is added. */
  public boolean isReady() {
    return safety.isSafe() && !faults.hasFault() && homingState != HomingState.FAULT;
  }

  public void requestManualOverride(boolean requested) {
    safety.requestManualOverride(requested);
  }

  public void clearFaults() {
    faults.clearLatchedFaults();
  }

  public void activate() {
    requestedActive = true;
  }

  public void deactivate() {
    requestedActive = false;
  }
}
