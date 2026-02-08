# PR: Config Service API & Robot Dispatcher Refactor

## Summary

This PR introduces two major coordinated changes to the BiemhTek2026 repository:

1. **Config Service**: A new Spring Boot microservice with PostgreSQL backend for managing robot programs and workpiece positions via REST API
2. **Robot Dispatcher**: A refactored robot-side execution model that replaces the switch-based program execution with a dispatcher/registry pattern supporting asynchronous vision tasks

## Changes Overview

### 1. Config Service (New Microservice)

**Location**: `config-service/`

A Spring Boot 2.7 application providing REST API for:
- Program configuration management (CRUD operations)
- Workpiece position tracking from vision system
- Health monitoring endpoints

**Key Features**:
- PostgreSQL database with Flyway migrations
- Docker support with docker-compose configuration
- Profile-based configuration via environment variables
- Pre-populated with sample program definitions (1-7)

**Technologies**:
- Spring Boot 2.7.18
- Spring Data JPA
- PostgreSQL 14
- Flyway for database migrations
- Docker & Docker Compose

### 2. Robot Dispatcher System (Refactored)

**Location**: `src/biemhTekniker/`

Replaces the traditional switch statement in `Main.java` with a dynamic dispatcher that:
- Loads program configurations from config service at runtime
- Distinguishes VISION vs ROBOT program types
- Executes vision tasks asynchronously (non-blocking)
- Executes robot tasks synchronously on main thread
- Posts workpiece positions back to config service

**Key Components**:
- `ProgramRegistry`: HTTP client for config service
- `ProgramDispatcher`: Routing and execution coordinator
- `ProgramTask` interface: Base for all tasks
- `VisionTask`/`RobotTask`: Abstract base classes
- `GetNewWorkpiecePositionTask`: Refactored vision task example
- Java 7 compatible HTTP and JSON utilities

## How to Run

### Running Config Service Locally

From repository root:

```bash
# Start PostgreSQL and Config Service
docker-compose up -d

# Check service health
curl http://localhost:8080/health

# View logs
docker-compose logs -f config-service

# Stop services
docker-compose down
```

### Configuring Robot to Use Config Service

1. Update `CONFIG_SERVICE_URL` in `src/biemhTekniker/Main.java`:

```java
private static final String CONFIG_SERVICE_URL = "http://172.31.1.100:8080";
```

2. Rebuild and deploy to robot controller

3. The robot will now:
   - Load program configurations from config service
   - Execute vision tasks asynchronously
   - Post workpiece positions to config service

### Running Without Config Service (Fallback Mode)

If `CONFIG_SERVICE_URL` is empty or null, the system operates in fallback mode with hardcoded program definitions.

## API Endpoints

### Programs

- `GET /api/programs` - List all programs
- `GET /api/programs/{number}` - Get specific program
- `POST /api/programs` - Create new program
- `PUT /api/programs/{number}` - Update program
- `DELETE /api/programs/{number}` - Delete program

### Workpieces

- `POST /api/workpieces` - Create workpiece position
- `GET /api/workpieces/latest` - Get latest workpiece
- `GET /api/workpieces` - List all workpieces
- `GET /api/workpieces/{id}` - Get specific workpiece

### Health

- `GET /health` - Service health check

## Database Schema

### Programs Table

```sql
CREATE TABLE programs (
    id BIGSERIAL PRIMARY KEY,
    program_number INTEGER NOT NULL UNIQUE,
    program_name VARCHAR(255) NOT NULL,
    program_type VARCHAR(50) NOT NULL,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Workpiece Positions Table

```sql
CREATE TABLE workpiece_positions (
    id BIGSERIAL PRIMARY KEY,
    x DOUBLE PRECISION NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    z DOUBLE PRECISION NOT NULL,
    rx DOUBLE PRECISION NOT NULL,
    ry DOUBLE PRECISION NOT NULL,
    rz DOUBLE PRECISION NOT NULL,
    score DOUBLE PRECISION,
    source_program VARCHAR(255),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL
);
```

## Architecture Diagram

```
┌─────────────────┐
│  Robot (Main)   │
│                 │
│  ┌───────────┐  │      HTTP      ┌──────────────────┐
│  │Dispatcher │◄─┼────────────────►│  Config Service  │
│  └─────┬─────┘  │   (REST API)   │   (Spring Boot)  │
│        │        │                 └────────┬─────────┘
│  ┌─────▼─────┐  │                          │
│  │  Vision   │  │                          │
│  │   Tasks   │  │                 ┌────────▼─────────┐
│  │  (Async)  │  │                 │    PostgreSQL    │
│  └───────────┘  │                 │    Database      │
│                 │                 └──────────────────┘
│  ┌───────────┐  │
│  │  Robot    │  │
│  │   Tasks   │  │
│  │  (Sync)   │  │
│  └───────────┘  │
└─────────────────┘
```

## Migration Path

### Existing Programs (1-7)

| Program | Name | Type | Implementation |
|---------|------|------|----------------|
| 1 | Get New Workpiece Position | VISION | ✅ Refactored as VisionTask |
| 2 | Calibration | VISION | 🔄 Legacy wrapper |
| 3 | Test Calibration | VISION | 🔄 Legacy wrapper |
| 4 | Pick New Workpiece | ROBOT | 🔄 Legacy wrapper |
| 5 | Place New Workpiece | ROBOT | 🔄 Legacy wrapper |
| 6 | Pick Measured Workpiece | ROBOT | 🔄 Legacy wrapper |
| 7 | Place Measured Workpiece | ROBOT | 🔄 Legacy wrapper |

Programs 2-7 currently use legacy wrappers that call existing implementations. These work correctly but can be refactored into proper tasks in future PRs.

## Benefits

### Immediate Benefits

1. **Non-blocking Vision**: Vision operations no longer block robot motion
2. **Dynamic Configuration**: Change program behavior without recompilation
3. **Position Tracking**: All workpiece positions stored in database
4. **API Access**: External systems can query and manage programs

### Long-term Benefits

1. **Scalability**: Easy to add new program types
2. **Monitoring**: Historical data for analysis and optimization
3. **Integration**: Other services can integrate via REST API
4. **Testing**: Easier to test individual tasks in isolation

## Testing Notes

### Config Service

Tested with Docker Compose:
- PostgreSQL connection and migrations ✅
- Health endpoint responding ✅
- CRUD operations for programs ✅
- Workpiece position storage ✅

### Robot Dispatcher

Manual testing required on actual hardware:
- Program loading from config service
- Async vision task execution
- Sync robot task execution
- Workpiece position posting

## Security Considerations

**Note**: API key authentication is prepared but not enforced in this initial version. All endpoints accept an optional `X-API-KEY` header but do not validate it. This should be implemented before production deployment.

## Java 7 Compatibility

All robot-side code is Java 7 compatible:
- No lambdas or streams
- Uses `HttpURLConnection` instead of modern HTTP clients
- Manual JSON parsing without external dependencies
- Anonymous inner classes for callbacks

## Documentation

- [Config Service README](config-service/README.md) - Service documentation
- [Robot Dispatcher README](ROBOT_DISPATCHER_README.md) - Dispatcher usage guide
- Inline code documentation throughout

## Future Work

Potential enhancements for future PRs:

1. **API Authentication**: Implement X-API-KEY validation
2. **Full Task Refactor**: Convert all legacy wrappers to proper tasks
3. **Error Recovery**: Add retry logic for HTTP failures
4. **Metrics Collection**: Track execution times and success rates
5. **Configuration File**: External config file instead of hardcoded URL
6. **Additional Endpoints**: Server management, system status, etc.

## Breaking Changes

None. The system falls back to hardcoded programs if config service is unavailable.

## Dependencies

### Config Service
- Spring Boot 2.7.18
- PostgreSQL driver
- Flyway
- Spring Data JPA
- Spring Actuator

### Robot Side
No new external dependencies (Java 7 standard library only)

## Files Changed

### Added
- `config-service/` (complete Spring Boot project)
- `docker-compose.yml`
- `src/biemhTekniker/model/` (ProgramType, ProgramDescriptor)
- `src/biemhTekniker/tasks/` (Task interfaces and implementations)
- `src/biemhTekniker/registry/ProgramRegistry.java`
- `src/biemhTekniker/dispatcher/` (Dispatcher and factory)
- `src/biemhTekniker/util/` (HTTP and JSON utilities)
- `ROBOT_DISPATCHER_README.md`

### Modified
- `src/biemhTekniker/Main.java` (dispatcher integration)
- `.gitignore` (exclude config-service build artifacts)

### Unchanged
- All existing program implementations (backward compatible)
- Vision system integration
- Console server
- All other robot functionality

## Deployment Steps

1. Deploy config service:
   ```bash
   docker-compose up -d
   ```

2. Update robot code:
   - Set `CONFIG_SERVICE_URL` in Main.java
   - Build and sync to robot controller

3. Test programs 1-7 work as before

4. Monitor logs for any HTTP errors

## Rollback Plan

If issues arise:
1. Set `CONFIG_SERVICE_URL = ""` in Main.java
2. Rebuild and redeploy
3. System reverts to switch-based execution

## Questions?

Refer to:
- [Config Service README](config-service/README.md)
- [Robot Dispatcher README](ROBOT_DISPATCHER_README.md)
- Inline code documentation
