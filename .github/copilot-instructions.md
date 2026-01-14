# GitHub Copilot Instructions for KUKA LBR iiwa Robotics Project

## Project Overview
This is a KUKA LBR iiwa 14 R820 industrial robotics project using the KUKA Sunrise platform. The robot is integrated into a manufacturing cell with PLC control, vision systems, and measurement peripherals.

## Critical Technical Constraints

### Java Version Constraint
- **MANDATORY**: Use Java 1.7 or lower syntax and features
- No Java 8+ features (lambdas, streams, Optional, try-with-resources, etc.)
- No method references or default interface methods
- Use explicit generic type parameters (no diamond operator <>)
- Documentation and examples reference: `/Documentation/KUKA_SunriseOS_116_END_en.pdf`

### KUKA Robotics API
- Base application classes extend `RoboticsAPIApplication`
- Use dependency injection with `@Inject` annotation for robot and I/O access
- Robot device: `LBR` (specifically LBR iiwa 14 R820)
- Motion commands: Use `BasicMotions.*` (ptp, lin, spl, etc.)
- Always respect robot safety and motion constraints

## Architecture & Design Principles

### Separation of Concerns
Maintain strict separation between three core layers:

1. **Hardware Communication Layer**
   - PLC communication via Profinet (generated IOAccess classes)
   - TCP/IP socket communication with vision PC
   - I/O signal handling
   - Keep hardware-specific code isolated

2. **Data Parsing Layer**
   - Parse string-based data received from external systems
   - Convert raw data to structured coordinate/command objects
   - Use skeleton/placeholder parsing structures until delimiters are defined
   - Validate and sanitize input data

3. **Motion Logic Layer**
   - Robot motion planning and execution
   - Coordinate transformations
   - Path planning and trajectory generation
   - Keep motion logic independent of communication details

### Code Organization
- Place application logic in `src/application/` package
- Generated I/O access classes are in `src/com/kuka/generated/ioAccess/`
- **NEVER** modify generated I/O classes (marked with "do not modify" warnings)
- Use descriptive class and method names
- Minimize global state; prefer passing parameters

## Hardware Integration

### PLC Communication (Profinet)
- **PLC Role**: Acts as the cell master controller
- **Protocol**: Profinet fieldbus communication
- **Signal Definitions**: Located in `/generatedFiles/IODescriptions/` (Work in Progress)
- **Generated Classes**: I/O access classes in `src/com/kuka/generated/ioAccess/`

#### Available I/O Groups
- `PlcRequestsProgramIOGroup` - Program control signals (Start, Stop, Abort)
- `PlcRequestsGrippersIOGroup` - Gripper control requests
- `ProgramDataIOGroup` - Program data exchange
- `ProgramStatusIOGroup` - Program status reporting
- `RobotStatusIOGroup` - Robot state information
- `Gripper1IOGroup`, `Gripper2IOGroup` - Gripper I/O
- `GripperStatusIOGroup` - Gripper status monitoring
- `PhotoneoIOGroup` - Vision system integration
- `MediaFlangeIOGroup` - Media flange I/O
- `AutomaticExchangeIOGroup` - Automatic exchange operations

#### PLC Integration Guidelines
- Use injected IOGroup instances to access I/O signals
- Check signal states before executing operations
- Implement proper handshaking protocols with PLC
- Handle I/O timeouts and error conditions gracefully

### Vision PC Communication (TCP/IP)
- **Robot Role**: Acts as TCP/IP client
- **Server**: Windows PC running vision system
- **Data Format**: String-based coordinate data
- **Protocol**: Custom socket protocol (specification in development)

#### TCP/IP Integration Guidelines
- Implement robust socket connection handling with reconnection logic
- Use try-catch blocks for all socket operations (no try-with-resources in Java 1.7)
- Implement timeouts for socket read/write operations
- Parse received strings into coordinate structures
- Validate coordinate data before use in motion commands
- Close sockets properly in finally blocks

#### Data Parsing Pattern (Skeleton Structure)
Since delimiters are not yet defined, use this approach:
```java
public class DataParser {
    // Placeholder for future delimiter definition
    private static final String DELIMITER = ";"; // TBD
    
    public Coordinates parseCoordinateString(String data) {
        // Skeleton structure - implement based on final protocol
        Coordinates coords = new Coordinates();
        
        // TODO: Parse data using defined delimiter
        // String[] parts = data.split(DELIMITER);
        // coords.setX(Double.parseDouble(parts[0]));
        // coords.setY(Double.parseDouble(parts[1]));
        // coords.setZ(Double.parseDouble(parts[2]));
        
        return coords;
    }
}
```

## System Context & Peripherals

### Integrated Systems
1. **Vision System (Photoneo)** - 3D vision for object detection and localization
   - May interface via PLC or direct TCP/IP
   - Provides coordinate data for pick operations

2. **Measurement Systems** - Quality control and verification
   - Interface through PLC I/O
   - Provide measurement results and verification signals

3. **Gripper Systems** - Two gripper systems available
   - Controlled via PLC I/O groups
   - Monitor grip status and force feedback

4. **Media Flange** - Tool-side I/O interface
   - Provides signals for end-effector peripherals
   - Managed through MediaFlangeIOGroup

### Integration Architecture
```
┌─────────────────────────────────────────────────┐
│                   PLC (Cell Master)              │
│              (Profinet Communication)            │
└─────────┬───────────────────────────┬───────────┘
          │                           │
          │ Profinet                  │ Profinet/Digital I/O
          │                           │
┌─────────▼───────────┐      ┌────────▼─────────────┐
│  KUKA LBR iiwa      │      │   Measurement        │
│  14 R820 Robot      │      │   Systems            │
│                     │      └──────────────────────┘
│  ┌───────────────┐  │
│  │ Sunrise OS    │  │
│  │ Java 1.7 App  │  │
│  └───────────────┘  │
└─────────┬───────────┘
          │
          │ TCP/IP Socket (Client)
          │
┌─────────▼───────────┐
│   Vision PC         │
│   (Windows)         │
│   - Photoneo 3D     │
│   - Custom Server   │
└─────────────────────┘
```

## Coding Best Practices

### Error Handling
- Use try-catch-finally blocks appropriately
- Log errors with context using `getLogger().error()` or `.warn()`
- Handle robot motion exceptions gracefully
- Implement proper cleanup in finally blocks
- Never swallow exceptions silently

### Motion Programming
- Always validate positions before motion commands
- Use appropriate motion types: PTP for point-to-point, LIN for linear, SPL for spline
- Set velocity and acceleration parameters explicitly
- Use `lbr.move()` for blocking motion, async motion for concurrent operations
- Consider collision avoidance and workspace limits
- Test motions in simulation before hardware execution

### Resource Management
- Close sockets, streams, and connections in finally blocks (no try-with-resources)
- Release robot motion control properly
- Clean up I/O resources in dispose() method
- Override dispose() and always call `super.dispose()`

### Comments and Documentation
- Document public APIs with JavaDoc
- Explain complex algorithms and coordinate transformations
- Mark TODO items for incomplete parsing/communication logic
- Document units for coordinates, velocities, and forces
- Explain safety-critical sections

### Testing Strategy
- Test hardware communication independently from motion logic
- Mock I/O groups for unit testing where possible
- Use simulation mode for initial motion testing
- Validate data parsing with various input formats
- Test error handling and recovery scenarios

### Testing & Validation Policy
- **No Active Testing**: Do not attempt to run, execute, or compile the code using automated test runners. The code requires a physical KUKA controller and a PLC.
- **Static Analysis Only**: Focus on structural and syntactical correctness for Java 1.7.
- **Manual Verification**: All functional testing is performed manually on physical hardware.

### Token Efficiency & Interaction Rules
- **Direct Answers**: Provide succinct code snippets and logic explanations. Avoid conversational filler or "sugar-coating."
- **Verification**: If a technical detail regarding KUKA safety or specific taxation-related logic is unclear, state that you are unsure rather than hallucinating based on training data.
- **Contextual Awareness**: Before generating code, check the `/Documentation` directory to ensure the methods used exist in the Sunrise 1.16 API.


### State Ownership & Handshaking
- **PLC as Source of Truth**: The PLC is the cell master. The robot must not initiate motion or change its internal state (e.g., "Task Complete") without a confirmed handshake via Profinet.
- **Sync Logic**: Use a "Request-Response" pattern for all external communication. The robot sends a request, waits for a status bit from the PLC or a string from the Vision PC, and validates the response before proceeding.


### Field Debugging Standards
- **Logging**: Implement verbose logging using `getLogger().info()`. Logs should clearly indicate state transitions (e.g., "TCP_CLIENT_CONNECTED", "PICK_COORDS_RECEIVED", "PLC_READY_FOR_PICK").
- **Language**: Use English for all code, variable names, and log messages to maintain consistency across the international project team.
- **Recovery**: Every motion sequence must have a corresponding "Safety Recovery" or "Abort" state defined in case the PLC sends a stop signal or the TCP socket times out.

## Common Patterns

### Robot Application Template
```java
package application;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import javax.inject.Inject;

public class MyApplication extends RoboticsAPIApplication {
    @Inject
    private LBR lbr;
    
    @Override
    public void initialize() {
        // Initialize resources, connections, I/O
    }
    
    @Override
    public void run() {
        try {
            // Main application logic
        } catch (Exception e) {
            getLogger().error("Application error", e);
        }
    }
    
    @Override
    public void dispose() {
        // Clean up resources
        super.dispose();
    }
}
```

### I/O Access Pattern
```java
@Inject
private PlcRequestsProgramIOGroup plcRequests;

public void waitForStartSignal() {
    while (!plcRequests.getProgramStart()) {
        // Wait for PLC start signal
        ThreadUtil.milliSleep(100);
    }
}
```

### Safe Motion Pattern
```java
public void moveToPosition(Frame targetFrame) {
    try {
        PTP motion = ptp(targetFrame);
        motion.setJointVelocityRel(0.25); // 25% of max velocity
        lbr.move(motion);
    } catch (Exception e) {
        getLogger().error("Motion failed", e);
        // Handle error, possibly return to safe position
    }
}
```

## Priority Checklist for Code Generation

When generating or reviewing code, prioritize:

1. ✅ Java 1.7 compatibility (no Java 8+ features)
2. ✅ Separation of concerns (communication/parsing/motion)
3. ✅ Proper error handling and logging
4. ✅ Resource cleanup (finally blocks)
5. ✅ Motion safety (velocity limits, position validation)
6. ✅ Clear TODOs for undefined protocols/delimiters
7. ✅ Never modify generated I/O classes
8. ✅ Document hardware integration points
9. ✅ Use skeleton structures for incomplete specifications
10. ✅ Follow existing code style and patterns

## References
- KUKA Sunrise Documentation: `/Documentation/KUKA_SunriseOS_116_END_en.pdf`
- I/O Descriptions: `/generatedFiles/IODescriptions/`
- Generated I/O Classes: `src/com/kuka/generated/ioAccess/`
- Example Applications: `src/application/`

---

**Note**: This is a living document. Update these instructions as the project evolves, protocols are finalized, and new integration requirements emerge.
