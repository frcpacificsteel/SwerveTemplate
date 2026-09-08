package frc.robot.controls;

import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.config.ControlConstants;

/** Maps the operator controller to game-neutral superstructure intent. */
public class OperatorControls {
  private final CommandXboxController controller = new CommandXboxController(ControlConstants.OPERATOR_CONTROLLER_PORT);

  public Trigger primaryAction() { return controller.a(); }
  public Trigger stow() { return controller.b(); }
  public Trigger manualOverride() { return controller.start(); }
  public Trigger clearFaults() { return controller.back(); }
  public boolean isConnected() { return controller.getHID().isConnected(); }
}
