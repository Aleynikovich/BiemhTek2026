# Robot Dispatcher and Task System

This document explains the new dispatcher-based program execution system that replaces the traditional switch-based approach in Main.java.

## Architecture Overview

The new system separates program execution into three key components:

1. **ProgramRegistry**: Loads program configurations from the config service API
2. **ProgramDispatcher**: Routes programs to appropriate handlers and manages execution
3. **ProgramTask**: Individual task implementations (VISION or ROBOT types)

### Key Benefits

- **Asynchronous Vision Tasks**: Vision programs run in the background, allowing robot motion to continue
- **Dynamic Configuration**: Programs can be configured via REST API without recompiling
- **Separation of Concerns**: Clear distinction between vision and robot operations
- **Extensibility**: Easy to add new program types and handlers

## Program Types

### VISION Programs
- Execute **asynchronously** (non-blocking)
- **Cannot** perform robot motions (no iiwa.move() calls)
- Typically interact with the vision system
- Examples: GetNewWorkpiecePosition, Calibration

### ROBOT Programs
- Execute **synchronously** on the main thread (blocking)
- **Can** perform robot motions using iiwa APIs
- Examples: PickNewWorkpiece, PlaceNewWorkpiece

## Configuration

### Config Service URL

Set the config service URL in `Main.java`:

```java
private static final String CONFIG_SERVICE_URL = "http://172.31.1.100:8080";
```

If left empty, the system operates in **fallback mode** with hardcoded program definitions.

### Program Registration

Programs are automatically loaded from the config service at runtime. The config service should return program descriptors in JSON format:

```json
{
  "id": 1,
  "programNumber": 1,
  "programName": "Get New Workpiece Position",
  "programType": "VISION",
  "description": "Captures new workpiece position from vision system",
  "enabled": true
}
```

## How It Works

### 1. Program Execution Flow

```
Main.run() 
  → Dispatcher.dispatch(programNumber)
    → Registry.getProgram(programNumber)
      → Config Service API call
    → TaskFactory.createTask(descriptor)
    → Execute task (sync or async based on type)
```

### 2. Vision Task Example (Asynchronous)

```java
public class GetNewWorkpiecePositionTask extends VisionTask {
    @Override
    public TaskResult execute() throws Exception {
        // 1. Communicate with vision system
        // 2. Parse results
        // 3. Post to config service
        // 4. NO robot motions!
        return TaskResult.success("Position retrieved");
    }
}
```

### 3. Robot Task Example (Synchronous)

```java
public class PickWorkpieceTask extends RobotTask {
    @Override
    public TaskResult execute() throws Exception {
        // 1. Get workpiece data
        // 2. Calculate approach
        // 3. Perform robot motion (iiwa.move())
        return TaskResult.success("Pick completed");
    }
}
```

## Adding New Programs

### Step 1: Create Program in Config Service

```bash
curl -X POST http://localhost:8080/api/programs \
  -H "Content-Type: application/json" \
  -d '{
    "programNumber": 10,
    "programName": "Custom Program",
    "programType": "VISION",
    "description": "My custom program",
    "enabled": true
  }'
```

### Step 2: Implement Task Class

Create a new task class extending either `VisionTask` or `RobotTask`:

```java
package biemhTekniker.tasks;

public class CustomProgramTask extends VisionTask {
    public CustomProgramTask() {
        super(10, "CustomProgram");
    }
    
    @Override
    public TaskResult execute() throws Exception {
        // Your implementation here
        return TaskResult.success("Custom program completed");
    }
}
```

### Step 3: Register in TaskFactory

Update `DefaultProgramTaskFactory.java`:

```java
@Override
public ProgramTask createTask(ProgramDescriptor descriptor) {
    switch (descriptor.getProgramNumber()) {
        case 10:
            return new CustomProgramTask();
        // ... other cases
    }
}
```

### Step 4: Test

```java
// From console or HMI, set program number to 10
main.setProgramNumber(10);
```

## Migrating Existing Programs

The existing programs (1-7) have been migrated to the new system:

| Program # | Name | Type | Status |
|-----------|------|------|--------|
| 1 | Get New Workpiece Position | VISION | ✅ Refactored as VisionTask |
| 2 | Calibration | VISION | ✅ Legacy wrapper |
| 3 | Test Calibration | VISION | ✅ Legacy wrapper |
| 4 | Pick New Workpiece | ROBOT | ✅ Legacy wrapper |
| 5 | Place New Workpiece | ROBOT | ✅ Legacy wrapper |
| 6 | Pick Measured Workpiece | ROBOT | ✅ Legacy wrapper |
| 7 | Place Measured Workpiece | ROBOT | ✅ Legacy wrapper |

**Note**: Programs 2-7 currently use "legacy wrappers" that call the original program implementations. These can be refactored into proper tasks over time.

## API Integration

### Fetching Programs

The `ProgramRegistry` automatically fetches programs from:
```
GET {CONFIG_SERVICE_URL}/api/programs/{program_number}
```

Response is cached to minimize HTTP requests. Call `registry.refresh()` to clear cache.

### Posting Workpiece Positions

Vision tasks can post workpiece positions to:
```
POST {CONFIG_SERVICE_URL}/api/workpieces
```

With payload:
```json
{
  "x": 300.0,
  "y": -320.0,
  "z": 200.0,
  "rx": -180.0,
  "ry": 0.0,
  "rz": 45.0,
  "score": 0.95,
  "sourceProgram": "GetNewWorkpiecePosition"
}
```

## Troubleshooting

### Program Not Dispatching

1. Check config service is running: `curl http://localhost:8080/health`
2. Verify program exists: `curl http://localhost:8080/api/programs/1`
3. Check logs for registry errors
4. Ensure `CONFIG_SERVICE_URL` is set correctly

### Vision Task Blocking Robot

- Ensure vision tasks extend `VisionTask` (not `RobotTask`)
- Verify task does not call `iiwa.move()` or other blocking robot APIs
- Check dispatcher logs for execution type

### Legacy Programs Not Working

- Verify original program classes are still available
- Check that task factory includes wrapper for program number
- Review logs for task creation failures

## Performance Considerations

### HTTP Timeouts

Default timeouts in `SimpleHttpClient`:
- Connect timeout: 5 seconds
- Read timeout: 10 seconds

Adjust if needed for slower networks.

### Vision Executor

Uses a **single-threaded executor** to prevent concurrent camera requests. Only one vision task executes at a time.

### Program Cache

Programs are cached after first fetch. To force refresh:
```java
programRegistry.refreshProgram(1);  // Refresh specific program
programRegistry.refresh();          // Clear entire cache
```

## Java 7 Compatibility

All new code is Java 7 compatible:
- Uses `HttpURLConnection` instead of modern HTTP clients
- Manual JSON parsing (no Jackson/Gson dependency by default)
- No lambdas or streams
- Anonymous inner classes for callbacks

## Future Enhancements

Potential improvements:

1. **Full Task Refactor**: Convert all legacy wrappers to proper task implementations
2. **Retry Logic**: Add automatic retry for failed HTTP requests
3. **Health Monitoring**: Periodic config service health checks
4. **Metrics**: Track task execution times and success rates
5. **API Authentication**: Implement X-API-KEY validation
6. **Configuration File**: Load CONFIG_SERVICE_URL from external file or PLC

## References

- [Config Service README](../config-service/README.md) - API documentation
- [Main.java](./src/biemhTekniker/Main.java) - Dispatcher integration
- [ProgramDispatcher.java](./src/biemhTekniker/dispatcher/ProgramDispatcher.java) - Core dispatcher logic
