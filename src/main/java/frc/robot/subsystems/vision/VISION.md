# Vision subsystem

`LimelightVisionSubsystem` reads camera data, checks the heartbeat, filters pose estimates, and gives the drivetrain only measurements it considers trustworthy. `VisionEstimate` and `VisionHealth` are data snapshots used by the rest of the code.

If vision does not affect pose estimation, start with the `Vision/` NT4 fields: target visibility, trusted pose, tag count, and rejection reason. Then verify the Limelight NetworkTables name, camera mounting, robot orientation feed, AprilTag layout, pipeline, and time synchronization. A visible target is not automatically a trustworthy pose.
