# GitHub Copilot Instructions: KUKA LBR iiwa Project

## Repository Overview
This is a KUKA LBR iiwa 14 R820 robotics project using Sunrise OS. The project includes robot control applications, I/O integration with Profinet, gripper control, and PLC communication. The codebase is built on the KUKA Sunrise.Workbench framework and follows strict Java 1.7 compatibility requirements due to the embedded controller environment.

## 1. Technical Constraints
- **Java Version**: Strictly Java 1.7 or lower. Avoid all features from Java 8+ (No lambdas, streams, or diamond operators).
- **Concurrency**: Use background backgroundPrograms (see sunrise docs as reference on how to do background tasks in sunrise) for TCP/IP and PLC polling to ensure non-blocking robot motion.
- **Thread Safety**: Use `java.util.concurrent.atomic` classes (e.g., `AtomicBoolean`) or `volatile` variables for inter-thread data exchange.
  - Mark all static lazy-initialized fields as `volatile` for thread-safe lazy initialization
  - Use double-checked locking pattern for singletons: check null → synchronized block → check null again → initialize
  - Document thread-confinement when variables are only accessed by a single thread
  - Prefer `AtomicReference` and `AtomicBoolean` for lock-free concurrent operations
- **No Active Testing**: Do not attempt to run code or tests. Focus on static analysis and syntactical correctness for the Sunrise OS environment.

## 2. Architecture & Patterns
- **Separation of Concerns**: Keep Hardware (TCP/PLC), Parsing (String processing), and Motion (Robot API) in strictly separate classes.
- **External Configuration**: No hardcoding IP/Ports/Delimiters/Constants. Load all settings from a `.properties` file on the controller using `java.util.Properties`.
  - All tunable parameters must be externalized: timeouts, tolerances, retry delays, motion parameters
  - Use ConfigManager.getInstance() to access configuration with sensible defaults
  - Add error handling with fallback to defaults when parsing configuration values
- **PLC Ownership**: The PLC is the cell master. All robot motions must be gated by Profinet handshake signals.
- **Logging**: Implement a background "Telnet-style" `ServerSocket` to broadcast robot logs (`getLogger()`) to remote clients on a dedicated port.

## 3. Hardware Integration
- **Sunrise API**: Reference `/Documentation/KUKA_SunriseOS_116_END_en.pdf` for API methods. Use `@Inject` for robot and I/O access.
- **I/O Mapping**: Reference `/generatedFiles/IODescriptions/` for Profinet signal groups. Do not modify generated classes.
- **HMI Buttons**: Use `IApplicationData` to create SmartPad buttons for manual gripper control.

## 4. Interaction Protocol
- **Ambiguity**: Use `AskUserQuestion` if parameters or signal names are missing.
- **Verification**: Summarize complex logic (handshaking/threading) for "Go Ahead" before generating the implementation.

## 5. Setup and Build
- **Environment**: KUKA Sunrise.Workbench (Eclipse-based IDE) is required for development
- **Build Process**: Projects are built within Sunrise.Workbench and deployed to the KUKA controller
- **Dependencies**: All required libraries are provided by the Sunrise SDK in the `KUKAJavaLib` directory
- **Configuration Files**: Station configuration in `StationSetup.cat`, I/O configuration in `IOConfiguration.wvs`
- **No External Build Tools**: Do not suggest Maven, Gradle, or other build systems - they are incompatible with Sunrise

## 6. Testing and Deployment
- **Testing Approach**: Code must be tested on actual hardware or in Sunrise simulation environment
- **No Unit Tests**: Due to the embedded nature and hardware dependencies, traditional unit testing is not used
- **Validation**: Static code analysis and syntax checking are the primary validation methods
- **Deployment**: Applications are synced to the robot controller via Sunrise.Workbench

## 7. Code Style and Conventions
- **Naming**: Follow Java naming conventions (camelCase for variables/methods, PascalCase for classes)
- **Generated Code**: Never modify files in `com.kuka.generated.ioAccess` package - these are auto-generated
- **Injection**: Use `@Inject` annotation for dependency injection of robot controller and I/O groups
- **Error Handling**: Always implement proper exception handling for robot motions and I/O operations
  - Add try-catch blocks for NumberFormatException when parsing configuration values
  - Provide fallback to sensible defaults when configuration parsing fails
  - Log clear error messages indicating what failed and what fallback is being used
- **Input Validation**: Validate all external inputs (JSON commands, PLC signals, configuration values)
  - Check for null and empty strings before processing
  - Validate required fields exist in JSON/structured data
  - Provide helpful error messages indicating what is missing or invalid
- **Logging**: Use `getLogger()` method from RoboticsAPIApplication for all logging

## 8. Project Structure
- **`src/application/`**: Main robot application classes
- **`src/biemhTekniker/`**: Custom application implementations
  - `biemhTekniker/config/`: Configuration management (ConfigManager, ImpedanceConfig, FrameRepository)
  - `biemhTekniker/console/`: TCP console server for GUI communication
  - `biemhTekniker/managers/`: Core application logic (AppController, PLCManager, LoggingManager)
  - `biemhTekniker/programs/`: Robot and vision task execution
  - `biemhTekniker/vision/`: Vision system integration
  - `biemhTekniker/data/`: Shared thread-safe data structures (WorkpieceQueue)
  - `biemhTekniker/logger/`: Distributed logging system
- **`src/com/kuka/generated/`**: Auto-generated I/O access classes (do not modify)
- **`configs/`**: Configuration files - all tunable parameters in application.properties
- **`generatedFiles/`**: Generated configuration files and I/O descriptions
- **`Documentation/`**: Project documentation including Sunrise API reference PDF

## 9. Configuration Parameters (application.properties)
- **Vision Server**: `vision.server.ip`, `vision.server.port`, `vision.references`, `vision.zone`
- **Vision Connection**: `vision.retry.initial.delay.ms`, `vision.retry.max.delay.ms`, `vision.retry.backoff.multiplier`, `vision.connection.check.interval.ms`, `vision.max.consecutive.errors`
- **Console Server**: `console.server.port`
- **Motion Parameters**: `motion.joint.velocity`, `motion.delay.ms`, `motion.redundancy.offsets` (degrees, comma-separated), `motion.place.z.rotations` (degrees, comma-separated)
- **Impedance Control**: `impedance.enabled`, `impedance.stiffness.x/y/z` (N/m), `impedance.stiffness.a/b/c` (Nm/rad), `impedance.damping`
- **Workpiece Queue**: `workpiece.position.tolerance.mm`
- **Calibration**: `calibration.points.count`, `calibration.points.root`
- **Pick Parameters**: `pick.prepick.offset.z`