# GitHub Copilot Instructions: KUKA LBR iiwa Project

## Repository Overview
This is a KUKA LBR iiwa 14 R820 robotics project using Sunrise OS. The project includes robot control applications, I/O integration with Profinet, gripper control, and PLC communication. The codebase is built on the KUKA Sunrise.Workbench framework and follows strict Java 1.7 compatibility requirements due to the embedded controller environment.

## 1. Technical Constraints
- **Java Version**: Strictly Java 1.7 or lower. Avoid all features from Java 8+ (No lambdas, streams, or diamond operators).
- **Concurrency**: Use background backgroundPrograms (see sunrise docs as reference on how to do background tasks in sunrise) for TCP/IP and PLC polling to ensure non-blocking robot motion.
- **Thread Safety**: Use `java.util.concurrent.atomic` classes (e.g., `AtomicBoolean`) or `volatile` variables for inter-thread data exchange.
- **No Active Testing**: Do not attempt to run code or tests. Focus on static analysis and syntactical correctness for the Sunrise OS environment.

## 2. Architecture & Patterns
- **Separation of Concerns**: Keep Hardware (TCP/PLC), Parsing (String processing), and Motion (Robot API) in strictly separate classes.
- **External Configuration**: No hardcoding IP/Ports/Delimiters. Load all settings from a `.properties` file on the controller using `java.util.Properties`.
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
- **Logging**: Use `getLogger()` method from RoboticsAPIApplication for all logging

## 8. Project Structure
- **`src/application/`**: Main robot application classes
- **`src/biemhTekniker/`**: Custom application implementations
- **`src/com/kuka/generated/`**: Auto-generated I/O access classes (do not modify)
- **`generatedFiles/`**: Generated configuration files and I/O descriptions
- **`Documentation/`**: Project documentation including Sunrise API reference PDF