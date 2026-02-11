# BiemhTek2026 - KUKA LBR iiwa Robot Application

## Project Overview

BiemhTek2026 is an advanced automated bin-picking application for the KUKA LBR iiwa 14 R820 collaborative robot integrated with a SmartPicking vision system. The project implements an intelligent pick-measure-return workflow where the robot autonomously identifies, picks, measures, and returns workpieces to their original locations.

## System Architecture

### Hardware Components
- **Robot**: KUKA LBR iiwa 14 R820 (7-axis collaborative robot)
- **Vision System**: SmartPicking camera system for part recognition
- **Measuring Machine**: Schunk base with multiple placement positions
- **Gripper System**: Three gripper positions (A, B, C) for workpiece handling
- **Control System**: KUKA Sunrise.Workbench running on embedded controller

### Software Components
- **Robot Application**: Java 1.7-based Sunrise OS application
- **Vision Integration**: TCP/IP communication with SmartPicking server
- **Console Server**: Remote control and monitoring via TCP/IP (port 30001)
- **GUI Application**: Python-based control interface with real-time monitoring

## Key Features

### Asynchronous Robot-Vision Architecture
The system operates with independent robot and vision threads, allowing the camera to scan for new workpieces while the robot is performing other operations. This parallel processing maximizes efficiency and throughput.

### Multi-Reference Support
Handles three different part types (references) simultaneously:
- BIEMH26_105053 (Reference 1)
- BIEMH26_105055 (Reference 2)
- BIEMH26_105060 (Reference 3)

Each workpiece is tracked with its reference type and orientation (0° or 180°), allowing the system to adapt handling strategies based on physical characteristics.

### Workpiece Queue System
A thread-safe queue manages up to 6 workpieces (2 per reference type) through their complete lifecycle:
- **AVAILABLE**: Vision system found it, ready for picking
- **PICKED**: Robot has picked it up
- **MEASURING**: Placed on measuring machine
- **MEASURED**: Measurement complete, ready for removal
- **RETURNED**: Returned to original position in bin

### Position-Based Tracking
Workpieces are tracked by their physical position (±5mm tolerance), preventing duplicate entries and maintaining identity across multiple vision scans. The system also tracks which gripper location (A, B, or C) holds each workpiece.

### Impedance Control
The robot uses compliant motion control to safely handle workpieces and adapt to external forces. This is particularly important when grippers exert force on workpieces, allowing the robot to yield slightly rather than fighting the force.

### Calibration System
A 16-point calibration routine establishes the coordinate transformation between the vision system and robot, ensuring accurate pick positions. The calibration can be performed at startup or on-demand.

## Typical Workflow

1. **Initial Setup**
   - Operator powers on the robot and connects the GUI
   - System performs vision-robot calibration (if needed)
   - Camera scans the bin and identifies available workpieces

2. **Pick-Measure-Return Cycle**
   - Vision system scans bin and finds workpieces (up to 6 total)
   - Robot picks the highest-scoring available workpiece
   - Robot places workpiece on measuring machine
   - While measuring occurs, vision scans for next batch
   - Robot picks another workpiece
   - Robot removes first workpiece from measuring machine
   - Robot places second workpiece on measuring machine
   - Robot returns first workpiece to its original position in bin
   - Cycle continues indefinitely

3. **Adaptive Operation**
   - If pick fails with regular orientation, robot automatically tries 180° rotation
   - System maintains awareness of which gripper holds which workpiece
   - Queue automatically updates as workpieces are found, picked, measured, and returned

## GUI Control Interface

The Python GUI provides a tabbed interface for comprehensive system control:

### Robot Programs Tab
Execute motion programs including:
- Pick new workpiece from bin
- Place workpiece on measuring machine
- Remove measured workpiece from machine
- Return workpiece to original bin position
- Calibration routines
- Home position commands

### Vision Commands Tab
Control vision system operations:
- Load part references
- Switch between auto and calibration modes
- Capture images
- Locate containers and parts
- Execute full scanning sequences

### Workpieces Tab
Real-time workpiece database viewer showing:
- 2D visualization of working plane (700×400mm canvas)
- Workpiece positions with color-coding by reference type
- State indication (Available, Picked, Measuring, Measured, Returned)
- Gripper location tracking (A, B, or C)
- Position coordinates and quality scores
- Interactive treeview for detailed information

### Console Tab
Real-time log monitoring with:
- Adjustable log level filtering (DEBUG, INFO, WARN, ERROR)
- Connection status display
- Command history
- Selectable/copyable log text
- Pop-out console window option

## Configuration

All system parameters are externalized in `configs/application.properties`:

- **Vision Server**: IP address and port for camera system
- **Vision References**: Part type identifiers
- **Motion Parameters**: Velocities, delays, and offsets
- **Impedance Control**: Stiffness and damping values for compliant behavior
- **Calibration**: Number of points and frame locations
- **Workpiece Queue**: Position tolerance for duplicate detection

This external configuration allows system tuning without code changes.

## Safety Features

### Program Cancellation
Operators can safely cancel running programs at any time. The robot will:
- Stop the current motion
- Preserve any held workpiece (grippers remain closed)
- Return to home position
- Reset to idle state

### Impedance Control
The robot operates with controlled compliance, allowing it to:
- Yield to unexpected collisions
- Absorb forces from gripper operations
- Reduce impact forces during contact
- Provide gentle handling of workpieces

### Emergency Stop
The GUI provides immediate emergency stop capability through Program 0, halting all robot motion instantly.

## Technical Specifications

### Development Environment
- **Platform**: KUKA Sunrise.Workbench (Eclipse-based IDE)
- **Language**: Java 1.7 (strict compatibility with embedded controller)
- **Operating System**: KUKA Sunrise OS 1.16
- **Build System**: Integrated with Sunrise.Workbench (no external tools)

### Communication Protocols
- **Robot Console**: TCP/IP on port 30001 with JSON command protocol
- **Vision System**: TCP/IP on port 59002 with custom text protocol
- **Logging**: TCP broadcast to connected GUI clients

### Motion Control
- **Control Modes**: Position control with optional impedance overlay
- **Motion Types**: PTP (Point-to-Point) and LIN (Linear)
- **Velocity**: Configurable joint velocity (default 25%)
- **Tool Coordination**: Support for multiple TCP (Tool Center Point) definitions

## Project Benefits

### Operational Efficiency
- Parallel vision and robot operations maximize throughput
- Queue system enables continuous operation without waiting for scans
- Automatic retry with alternative orientations reduces pick failures

### Flexibility
- Support for multiple part types simultaneously
- Configurable parameters for easy adaptation
- Modular program structure allows easy extension

### Safety
- Collaborative robot with compliant motion control
- Safe cancellation of operations
- Impedance control prevents excessive forces

### Maintainability
- External configuration for all tunable parameters
- Centralized frame management
- Clear separation between vision and robot control
- Comprehensive logging for troubleshooting

## Future Capabilities

The system architecture supports future enhancements:
- Additional part reference types
- More complex motion sequences
- Enhanced vision processing
- Machine learning for pick strategy optimization
- Integration with additional sensors or equipment

## Contact and Support

**Project**: BiemhTek2026  
**Robot**: KUKA LBR iiwa 14 R820  
**Vision**: SmartPicking System  
**License**: Proprietary
