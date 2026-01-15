# Core Architecture Layer

All source code for the KUKA LBR iiwa project is in the `biemhTekniker` package.

## Components

### ConfigManager
Singleton configuration manager for loading robot and PLC properties from KRC drive.

### VisionFrame
Data class for parsing vision system data strings into structured coordinates.

### VisionClient (RoboticsAPIBackgroundTask)
Background TCP client for vision system communication.

### LoggingServer (RoboticsAPIBackgroundTask)
Background ServerSocket for streaming robot logs to remote Telnet clients.

### HeartbeatTask (RoboticsAPIBackgroundTask)
Background task that toggles a Profinet heartbeat signal every 100ms for PLC liveness indication.
NOTE: Currently uses RobotStatus.ZRes1 - this reserve bit should be renamed in IO configuration.

### GripperController
Controller for Gripper 1 and Gripper 2 with HMI button support for manual control.

### MeasurementGripperController (RoboticsAPIBackgroundTask)
PLC-based gripper request handler with rising-edge detection callbacks.

## Configuration

Configuration files are located in `/configs` directory and should be deployed to `/home/KRC/configs/` on the KUKA controller.

## Usage

See `Main.java` for integration with RoboticsAPIApplication.
