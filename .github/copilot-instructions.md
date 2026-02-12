# GitHub Copilot Instructions: KUKA LBR iiwa Project

## Repository Overview
This is a KUKA LBR iiwa 14 R820 robotics project using Sunrise OS. The project includes robot control applications, I/O integration with Profinet, gripper control, and PLC communication. The codebase is built on the KUKA Sunrise.Workbench framework and follows strict Java 1.7 compatibility requirements due to the embedded controller environment.

**Project Type**: Industrial robotics application (Java 1.7 + Python GUI)  
**Size**: ~50 Java files, ~10 Python files, ~8,000 lines of code  
**Key Technologies**: KUKA Sunrise OS, TCP/IP communication, vision system integration

## Quick Reference - Key Files
- **Main Application**: `src/application/Main.java` - Application entry point
- **App Controller**: `src/biemhTekniker/managers/AppController.java` - Core robot control logic
- **Console Server**: `src/biemhTekniker/console/ConsoleServer.java` - TCP command interface
- **Configuration**: `configs/application.properties` - All tunable parameters
- **Station Setup**: `StationSetup.cat` - KUKA station configuration (binary)
- **I/O Config**: `IOConfiguration.wvs` - Profinet I/O mapping (binary)
- **Documentation**: `README.md`, `PROJECT_HANDOVER.md`, `Documentation/KUKA_SunriseOS_116_END_en.pdf`
- **Generated I/O**: `src/com/kuka/generated/ioAccess/` - DO NOT MODIFY (auto-generated)


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
  - Java 1.7 JDK must be configured in Eclipse
  - Custom builders: `com.kuka.roboticsAPI.eclipseBase.sunriseBuilder` and standard `javabuilder`
- **Build Process**: 
  - Projects are built within Sunrise.Workbench using Eclipse's automatic build
  - Compiled output goes to `bin/` directory
  - DO NOT attempt to build via command line - this is IDE-managed only
  - Build happens automatically when files are saved in Sunrise.Workbench
- **Dependencies**: 
  - All required libraries are provided by the Sunrise SDK in the `KUKAJavaLib` directory
  - 11 JAR files are referenced in `.classpath` including `roboticsAPI.core.jar`, `roboticsAPI.communication.jar`, etc.
  - No external dependency management (no Maven/Gradle)
- **Configuration Files**: 
  - Station configuration in `StationSetup.cat`
  - I/O configuration in `IOConfiguration.wvs`
  - Application settings in `configs/application.properties`
  - Eclipse project config in `.project` and `.classpath`
- **No External Build Tools**: Do not suggest Maven, Gradle, or other build systems - they are incompatible with Sunrise

## 6. Testing and Deployment
- **Testing Approach**: 
  - Code must be tested on actual hardware or in Sunrise simulation environment
  - Manual testing through SmartPad HMI on the robot controller
  - GUI-based validation using Python control interface (Tkinter/Streamlit)
- **No Unit Tests**: 
  - Due to the embedded nature and hardware dependencies, traditional unit testing is not used
  - No JUnit, TestNG, or automated test frameworks
  - Test programs exist: `TestCalibrationProgram.java`, `TestKUKA.java` (backup)
- **No CI/CD**: 
  - No GitHub Actions, Jenkins, or other CI/CD pipelines
  - No automated builds or test runs
  - Quality assurance relies on Sunrise IDE validation and runtime testing
- **Validation Methods**:
  - Static code analysis and syntax checking in Sunrise.Workbench
  - Runtime logging through centralized LogManager and console server
  - Visual verification via GUI workpiece canvas and status displays
  - Calibration programs for vision-robot coordinate validation
- **Deployment**: 
  - Applications are synced to the robot controller via Sunrise.Workbench
  - No command-line deployment - use IDE's "Synchronize Project" feature
  - Configuration files must be copied to controller separately

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

## 9. Python GUI (Optional Component)
- **Location**: `gui/` directory contains Python control interface
- **Dependencies**: Install via `pip install -r requirements.txt` in the gui directory
- **Run Command**: `python robot_control_gui_streamlit.py` (Streamlit version) or `python robot_control_gui.py` (Tkinter version)
- **Purpose**: Remote monitoring and control of robot via TCP console server
- **Features**: Program execution, workpiece visualization, log monitoring, motion overrides

## 10. Configuration Parameters (application.properties)
- **Vision Server**: `vision.server.ip`, `vision.server.port`, `vision.references`, `vision.zone`
- **Vision Connection**: `vision.retry.initial.delay.ms`, `vision.retry.max.delay.ms`, `vision.retry.backoff.multiplier`, `vision.connection.check.interval.ms`, `vision.max.consecutive.errors`
- **Console Server**: `console.server.port`
- **Motion Parameters**: `motion.joint.velocity`, `motion.delay.ms`, `motion.redundancy.offsets` (degrees, comma-separated), `motion.place.z.rotations` (degrees, comma-separated)
- **Impedance Control**: `impedance.enabled`, `impedance.stiffness.x/y/z` (N/m), `impedance.stiffness.a/b/c` (Nm/rad), `impedance.damping`
- **Workpiece Queue**: `workpiece.position.tolerance.mm`
- **Calibration**: `calibration.points.count`, `calibration.points.root`
- **Pick Parameters**: `pick.prepick.offset.z`

## 11. Common Pitfalls and Critical Warnings
- **DO NOT** use Java 8+ features (lambdas, streams, Optional, try-with-resources, etc.) - they will compile but fail on the controller
- **DO NOT** modify files in `src/com/kuka/generated/ioAccess/` - they are regenerated from WorkVisual
- **DO NOT** attempt to build or run the Java code outside of Sunrise.Workbench - it requires the KUKA runtime
- **DO NOT** use command-line `git` operations on `.cat`, `.wvs`, or `.sconf` files - they are binary and managed by KUKA tools
- **ALWAYS** use `AtomicBoolean`, `AtomicReference`, or `volatile` for variables shared between threads
- **ALWAYS** check for null before accessing variables passed between threads
- **ALWAYS** externalize magic numbers to `application.properties` with sensible defaults
- **REMEMBER** the PLC owns the cell - never bypass Profinet handshake signals
- **REMEMBER** motion programs run in background threads - use proper synchronization

## 12. How to Verify Changes
Since there are no automated tests or CI/CD:
1. **Syntax Check**: Code must compile without errors in Sunrise.Workbench
2. **Static Analysis**: Review for Java 1.7 compatibility and thread safety
3. **Configuration Check**: Verify all new parameters are in `application.properties` with defaults
4. **API Check**: Reference `/Documentation/KUKA_SunriseOS_116_END_en.pdf` for correct API usage
5. **Manual Review**: Check that motion sequences respect PLC handshake requirements
6. **Runtime Testing**: Deploy to controller or simulator and test with GUI
7. **Log Review**: Monitor console output for errors or warnings during operation