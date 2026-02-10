# BiemhTek2026 - KUKA LBR iiwa Robot Application

Advanced bin-picking application for the KUKA LBR iiwa 14 R820 robot with SmartPicking vision system integration.

## Overview

This application implements an automated pick-measure-return workflow with full vision system integration. The architecture separates robot motion control from vision processing, enabling asynchronous operation for maximum efficiency.

## Key Features

- **Async Robot/Vision Architecture**: Robot and camera operate independently
- **Multi-Reference Support**: Handles 3 different part references simultaneously
- **Workpiece Queue System**: Thread-safe queue manages up to 6 workpieces (2 per reference)
- **Pick-Measure-Return Lifecycle**: Automated workflow for part inspection
- **External Configuration**: All settings in `configs/application.properties`
- **TCP/IP Console**: Remote control and monitoring via GUI

## Architecture

### Robot Thread (Main)
Executes physical robot motions and gripper operations. Programs 1-99 run on this thread and access the shared `WorkpieceQueue` for coordination.

### Vision Thread
Sends commands to the SmartPicking camera system. Programs 100-199 run asynchronously on this thread, populating the `WorkpieceQueue` with found workpieces.

### WorkpieceQueue (Shared State)
Thread-safe queue that stores workpiece data with lifecycle states:
- `AVAILABLE`: Camera found it, ready to pick
- `PICKED`: Robot picked it up
- `MEASURING`: Placed on measuring machine
- `MEASURED`: Ready to be removed from machine
- `RETURNED`: Returned to origin position in bin

## Program Numbers

### Robot Programs (1-99)
| Number | Name | Description |
|--------|------|-------------|
| 0 | Idle | No operation |
| 1 | Pick New Workpiece | Pick next available from queue |
| 2 | Place on Measuring Machine | Place held workpiece on machine |
| 3 | Remove Measured Workpiece | Remove measured part from machine |
| 4 | Return Measured to Bin | Return to original pick location |
| 5 | Calibration | Robot-vision calibration sequence |
| 6 | Test Calibration | Verify calibration accuracy |

### Vision Programs (100-199)
| Number | Name | Description |
|--------|------|-------------|
| 100 | Load References | Load all references from config |
| 101 | Set Auto Mode | Switch camera to auto mode |
| 102 | Set Calibration Mode | Switch camera to calibration mode |
| 103 | Capture Data | Capture image from camera |
| 104 | Locate Container | Find bin/container position |
| 105 | Get Container Position | Retrieve container coordinates |
| 106 | Locate Parts | Find parts for specific reference |
| 107 | Get Part Position | Get first part position |
| 108 | Get Next Part Position | Get next part in sequence |
| 109 | Full Scan Sequence | Complete scan across all references |
| 111 | Get New Workpiece Position | Legacy single-reference scan |

## Configuration

All settings are in `configs/application.properties`:

```properties
# Vision Server
vision.server.ip=172.31.1.69
vision.server.port=59002

# Vision References (3 part types)
vision.references=BIEMH26_105053,BIEMH26_105055,BIEMH26_105060
vision.reference.count=3
vision.zone=1

# Console Server
console.server.port=30001

# Motion Parameters
motion.joint.velocity=0.25
motion.delay.ms=500
motion.delay.minor.ms=200

# Calibration
calibration.points.count=16
calibration.points.root=/CalibrationPoints

# Pre-pick approach offset (mm)
pick.prepick.offset.z=100
```

## Workflow

### Typical Pick-Measure-Return Cycle

1. **Vision Scan** (Program 109): Camera scans bin, finds 6 workpieces (2 per reference)
2. **Pick Best** (Program 1): Robot picks highest-score available workpiece
3. **Place on Machine** (Program 2): Place workpiece on measuring machine
4. **Scan Again** (Program 109): Camera scans for next batch while measuring
5. **Pick Next** (Program 1): Robot picks next workpiece
6. **Remove Measured** (Program 3): Remove first workpiece from machine
7. **Place New on Machine** (Program 2): Place second workpiece on machine
8. **Return Measured** (Program 4): Return first workpiece to origin position in bin
9. **Repeat**: Continue cycle indefinitely

## Package Structure

```
src/biemhTekniker/
├── Main.java                      # Main application orchestrator
├── config/
│   ├── ConfigManager.java         # Configuration loader (singleton)
│   └── FrameRepository.java       # Centralized frame management (NEW)
├── console/
│   ├── ConsoleServer.java         # TCP/IP command server
│   ├── ConsoleServerInterface.java
│   ├── ConsoleCommandHandler.java # Command processor
│   └── SimpleJSON.java            # Lightweight JSON parser
├── data/
│   ├── WorkpieceData.java         # Single workpiece data (enhanced with gripper tracking)
│   ├── WorkpieceState.java        # Lifecycle state enum
│   └── WorkpieceQueue.java        # Thread-safe queue (position-based tracking)
├── logger/
│   └── ...                        # Logging infrastructure
├── managers/
│   └── LoggingManager.java        # Log system manager
├── programs/
│   ├── RobotProgram.java          # Robot program interface
│   ├── VisionTask.java            # Vision task interface
│   ├── RobotContext.java          # Robot dependencies (includes FrameRepository)
│   ├── VisionContext.java         # Vision dependencies
│   ├── ProgramDispatcher.java     # Program router (0-199)
│   ├── MotionStrategy.java        # Motion execution strategy
│   ├── Pick*.java                 # Pick programs
│   ├── Place*.java                # Place programs
│   ├── Calibration*.java          # Calibration programs
│   ├── LoadReferencesTask.java    # Vision task implementations
│   ├── FullScanTask.java          # Full scan with workpiece tracking
│   └── IndividualVisionCommandTask.java
├── vision/
│   ├── SmartPickingProtocol.java  # Camera protocol (flexible arguments)
│   ├── SmartPickingThread.java    # Camera connection thread
│   ├── VisionManager.java         # Vision task executor
│   └── VisionSocketClient.java    # TCP socket client
└── hmi/
    └── HmiButtonHandler.java      # SmartPad buttons (refactored with template pattern)
```

## GUI Control

Use `gui/robot_control_gui.py` for remote control:

```bash
python3 gui/robot_control_gui.py
```

Features:
- **Tabbed Interface**:
  - Robot Programs tab - Motion programs (1-6)
  - Vision Commands tab - Vision tasks (100-111)
  - Workpieces tab - Database viewer with gripper tracking
  - Console tab - Real-time log output with filtering
- Connect to robot console server
- Execute robot programs and vision commands
- Monitor workpiece queue with position and gripper location
- View queue status
- Real-time robot logs with level filtering
- Emergency stop (Program 0)
- Cancel program and return home

## Development

### Requirements
- KUKA Sunrise.Workbench
- Java 1.7 (strict - no Java 8+ features)
- KUKA LBR iiwa robot controller
- SmartPicking vision system

### Java 1.7 Constraints
This codebase must maintain Java 1.7 compatibility:
- No lambda expressions
- No try-with-resources
- No diamond operator for anonymous classes
- No Streams API
- Use `java.util.concurrent.atomic` for thread safety

### Building
Projects are built and deployed through Sunrise.Workbench. No external build tools (Maven, Gradle) are used.

### Testing
Testing occurs on actual hardware or in Sunrise simulation environment. Unit tests are not applicable due to hardware dependencies.

## Thread Safety

Critical thread-safe components:
- `WorkpieceQueue`: All methods `synchronized`, position-based tracking
- `WorkpieceData`: All getters/setters `synchronized`, includes gripper location
- `programNumber` in Main: declared `volatile`
- Vision tasks: Serialized through `VisionManager`

## Recent Refactoring (2026)

The codebase underwent comprehensive refactoring to improve maintainability and flexibility:

### Key Improvements
1. **Vision Protocol Enhancement**: Support for flexible subarguments (e.g., "4;1;2")
2. **Workpiece Tracking**: Position-based tracking (±5mm tolerance) prevents duplicate workpieces
3. **Frame Repository**: Centralized frame management separates station layout from business logic
4. **HMI Refactoring**: Template pattern reduces code duplication by 37%
5. **GUI Enhancement**: Tabbed interface with workpiece database viewer

See [REFACTORING_SUMMARY.md](REFACTORING_SUMMARY.md) for detailed information.

## License

Proprietary - BiemhTek2026 Project

## Contact

Project: BiemhTek2026
Robot: KUKA LBR iiwa 14 R820
Vision: SmartPicking System
