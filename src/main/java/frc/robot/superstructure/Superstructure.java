package frc.robot.superstructure;

import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringPublisher;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.mechanisms.TemplateMechanismSubsystem;

/** Coordinates high-level goals without exposing mechanism details to controls or autos. */
public class Superstructure extends SubsystemBase {
  private final TemplateMechanismSubsystem mechanism;
  private final StringPublisher goalPublisher;
  private final StringPublisher detailPublisher;
  private final BooleanPublisher readyPublisher;
  private SuperstructureGoal requestedGoal = SuperstructureGoal.STOW;

  public Superstructure(TemplateMechanismSubsystem mechanism) {
    this.mechanism = mechanism;
    NetworkTable table = NetworkTableInstance.getDefault().getTable("Superstructure");
    goalPublisher = table.getStringTopic("requestedGoal").publish();
    detailPublisher = table.getStringTopic("detail").publish();
    readyPublisher = table.getBooleanTopic("ready").publish();
  }

  public Command requestGoal(SuperstructureGoal goal) {
    return Commands.runOnce(() -> requestedGoal = goal, this);
  }

  public Command safeStopCommand() {
    return Commands.runOnce(() -> requestedGoal = SuperstructureGoal.STOW, this);
  }

  public Command clearFaultsCommand() {
    return Commands.runOnce(mechanism::clearFaults).ignoringDisable(true);
  }

  public void requestManualOverride(boolean requested) {
    mechanism.requestManualOverride(requested);
    if (!requested && requestedGoal == SuperstructureGoal.MANUAL_OVERRIDE) {
      requestedGoal = SuperstructureGoal.STOW;
    } else if (requested) {
      requestedGoal = SuperstructureGoal.MANUAL_OVERRIDE;
    }
  }

  public SuperstructureStatus getStatus() {
    boolean ready = mechanism.isReady();
    return new SuperstructureStatus(
        requestedGoal,
        ready,
        ready ? "READY" : "Template mechanism is not ready");
  }

  public boolean isReadyForAuto() {
    return getStatus().ready();
  }

  @Override
  public void periodic() {
    switch (requestedGoal) {
      case STOW -> mechanism.deactivate();
      case PRIMARY_ACTION -> mechanism.activate();
      case MANUAL_OVERRIDE -> mechanism.activate();
    }

    SuperstructureStatus status = getStatus();
    goalPublisher.set(status.requestedGoal().name());
    readyPublisher.set(status.ready());
    detailPublisher.set(status.detail());
  }
}
