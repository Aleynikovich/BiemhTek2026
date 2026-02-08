# KUKA Robot Configuration Service Integration

This repository contains both the KUKA robot application and a Spring Boot configuration service for managing robot programs and workpiece positions.

## Architecture Overview

The system is split into two main components:

1. **Config Service** - A Spring Boot REST API backed by PostgreSQL that manages:
   - Program configurations (program number, name, type, description)
   - Workpiece positions captured from the vision system

2. **Robot Application** - KUKA Sunrise application that:
   - Loads program descriptors from the config service
   - Dispatches programs based on type (VISION vs ROBOT)
   - Executes VISION tasks asynchronously (non-blocking)
   - Executes ROBOT tasks synchronously on the main thread

## Quick Start

### 1. Start the Config Service

From the repository root:

```bash
docker-compose up -d
```

This starts:
- PostgreSQL database on port 5432
- Config Service API on port 8080

The database is automatically initialized with the schema and sample programs (1-7).

### 2. Verify the Config Service

Check the health endpoint:

```bash
curl http://localhost:8080/health
```

List all programs:

```bash
curl http://localhost:8080/api/programs
```

### 3. Configure the Robot

In `src/biemhTekniker/Main.java`, update the `CONFIG_SERVICE_BASE_URL` constant:

```java
private static final String CONFIG_SERVICE_BASE_URL = "http://172.31.1.100:8080";
```

Replace `172.31.1.100` with the IP address of the machine running the config service.

**Note:** To disable the config service integration and use the legacy switch-based program execution, set this to an empty string:

```java
private static final String CONFIG_SERVICE_BASE_URL = "";
```

### 4. Deploy to Robot

Build and sync the robot application to the KUKA controller using Sunrise.Workbench as usual.

## How It Works

### Dispatcher Pattern

The robot application now uses a dispatcher/registry pattern instead of the old switch statement:

**Old Approach:**
```java
switch (programNumber) {
    case 1: getNewWorkpiecePosition(); break;
    case 2: executeCalibration(); break;
    // ...
}
```

**New Approach:**
```java
programDispatcher.dispatch(programNumber, () -> programNumber = 0);
```

The dispatcher:
1. Loads the program descriptor from the config service registry
2. Determines the program type (VISION or ROBOT)
3. Creates the appropriate task using registered factories
4. Executes VISION tasks asynchronously (returns immediately)
5. Executes ROBOT tasks synchronously (blocks until complete)

### Program Types

- **VISION Programs** (1-3): Interact with the vision system without robot motion
  - Executed asynchronously on a background thread
  - Do not block the main robot control loop
  - Example: `GetNewWorkpiecePositionTask`

- **ROBOT Programs** (4-7): Perform physical robot movements
  - Executed synchronously on the main thread
  - Block until the motion is complete
  - Example: Pick/Place operations

### Legacy Fallback

If the config service is not configured or not reachable, the robot application falls back to the legacy switch statement for backward compatibility.

## Config Service API

See [config-service/README.md](config-service/README.md) for detailed API documentation.

### Example: Adding a New Program

```bash
curl -X POST http://localhost:8080/api/programs \
  -H "Content-Type: application/json" \
  -d '{
    "programNumber": 10,
    "programName": "Custom Vision Task",
    "programType": "VISION",
    "description": "Custom vision processing task",
    "enabled": true
  }'
```

### Example: Viewing Latest Workpiece Position

```bash
curl http://localhost:8080/api/workpieces/latest
```

## Development

### Adding New Program Tasks

To add a new program task:

1. Create a new class extending `VisionTask` or `RobotTask`:

```java
public class MyCustomTask extends VisionTask {
    public MyCustomTask(ProgramDescriptor descriptor) {
        super(descriptor);
    }
    
    @Override
    public TaskResult execute() {
        // Your task implementation
        return TaskResult.success("Task completed");
    }
}
```

2. Register it in a custom factory or extend `DefaultProgramTaskFactory`:

```java
@Override
public ProgramTask createTask(ProgramDescriptor descriptor) {
    if (descriptor.getProgramNumber() == 10) {
        return new MyCustomTask(descriptor);
    }
    return super.createTask(descriptor); // Fallback to default
}
```

3. Register your factory in `Main.initialize()`:

```java
CustomFactory factory = new CustomFactory(...);
programDispatcher.registerFactory(factory);
```

### Java 7 Compatibility

The robot-side code is strictly Java 7 compatible for the KUKA Sunrise environment:
- No lambdas (use anonymous classes)
- No streams
- No diamond operators
- Simple HTTP client using `HttpURLConnection`
- Basic JSON parsing without external libraries

## File Structure

```
BiemhTek2026/
├── config-service/              # Spring Boot config service
│   ├── src/main/java/
│   │   └── com/biemh/configservice/
│   │       ├── ConfigServiceApplication.java
│   │       ├── controller/
│   │       ├── domain/
│   │       └── repository/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.yml           # Docker Compose for local dev
├── src/biemhTekniker/           # Robot application
│   ├── Main.java               # Main entry point (modified)
│   ├── dispatcher/             # Dispatcher pattern
│   ├── registry/               # Program registry & HTTP client
│   ├── tasks/                  # Task implementations
│   ├── model/                  # Data models
│   ├── programs/               # Legacy programs (gradual migration)
│   └── ...
└── README.md                    # This file
```

## Troubleshooting

### Config Service Not Reachable

If the robot application logs show connection errors to the config service:

1. Verify the config service is running: `docker-compose ps`
2. Check the IP address in `CONFIG_SERVICE_BASE_URL` is correct
3. Test connectivity from the robot controller: `ping 172.31.1.100`
4. Check firewall rules allow port 8080

### Vision Task Not Executing

1. Check the program descriptor has `programType: VISION` in the database
2. Verify vision server connection is active
3. Check logs for task execution errors

### Database Migration Errors

If Flyway migration fails:

```bash
# Stop services
docker-compose down

# Remove volumes
docker volume rm biemhtek2026_postgres-data

# Restart
docker-compose up -d
```

## Security Note

The current implementation includes placeholder comments for API key authentication (`X-API-KEY` header) but does not enforce it. For production deployment, implement proper authentication and authorization.

## Contributing

When contributing to the robot application:
- Maintain Java 7 compatibility
- Follow existing code style
- Keep VISION tasks free of robot motion code
- Test with both config service enabled and disabled (legacy fallback)
