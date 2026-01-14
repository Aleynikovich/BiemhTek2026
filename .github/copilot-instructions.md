# GitHub Copilot Instructions: KUKA LBR iiwa Project

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