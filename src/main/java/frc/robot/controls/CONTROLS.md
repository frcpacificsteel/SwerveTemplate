# Controls and bindings

`DriverControls` and `OperatorControls` describe physical controller input. `ControlBindings` connects that input to named robot actions.

Keep subsystems free of direct controller reads. When a button does not do what the driver expects, check the USB port in `ControlConstants`, controller connection status, the relevant binding, command requirements, and active safety blockers.
