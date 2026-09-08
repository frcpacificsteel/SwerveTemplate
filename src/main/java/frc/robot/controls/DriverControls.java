package frc.robot.controls;

import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.config.ControlConstants;

/** Maps driver hardware to named robot intent; no subsystem should access a HID directly. */
public class DriverControls {
  private final CommandXboxController controller = new CommandXboxController(ControlConstants.DRIVER_CONTROLLER_PORT);

  public double driveForward() { return -controller.getLeftY(); }
  public double driveLeft() { return -controller.getLeftX(); }
  public double rotate() { return -controller.getRightX(); }

  public Trigger brake() { return controller.x(); }
  public Trigger resetFieldHeading() { return controller.back(); }
  public Trigger pathfindToTarget() { return controller.y(); }
  public Trigger selectLeftTarget() { return controller.povLeft(); }
  public Trigger selectRightTarget() { return controller.povRight(); }
  public Trigger selectMiddleTarget() { return controller.povDown(); }
  public Trigger selectNextAuto() { return controller.povUp(); }
  public Trigger selectPreviousAuto() { return controller.povDown(); }
  public Trigger runPrimaryGoal() { return controller.b(); }
  public boolean isConnected() { return controller.getHID().isConnected(); }
}
