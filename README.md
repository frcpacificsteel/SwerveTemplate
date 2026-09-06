# FRC Team 5025 - Pacific Steel Swerve Template

This repository contains the software codebase for Team 5025 (Pacific Steel). It provides the core framework for controlling the robot, managing swerve drive kinematics, operating sub-mechanisms, executing autonomous routines, and processing vision data.

## Overview

The code is structured around the WPILib Command-Based paradigm. Main application logic resides in `src/main/java/frc/robot`, with dedicated packages for constants, subsystems, telemetry, and automated state management.

### Key Components

- Drivetrain & Kinematics: Swerve drive control integrated with CTRE Phoenix 6 and PathPlanner libraries.
- Subsystems & Mechanisms: Modular components representing physical robot hardware and actuators.
- State Management: High-level state handling and automated pathfinding logic.
- Vision & Telemetry: Sensor integrations and Dashboard monitoring tools.

## Prerequisites & Tools

- Java Development Kit (JDK 17 or higher)
- WPILib Development Suite
- Gradle build tool (included wrapper `./gradlew`)
- Driver Station & SmartDashboard / Shuffleboard

## Build and Deployment Commands

To build the robot project:
```bash
./gradlew build
```

To deploy code to the roboRIO:
```bash
./gradlew deploy
```

To run in simulation mode:
```bash
./gradlew simulateJava
```

## Repository Guide

For detailed setup instructions, hardware mapping, controller configuration, and guides on modifying code for new mechanisms, refer to `REPOSITORY_GUIDE.md` located in the root directory.
