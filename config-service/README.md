# Config Service

REST API service for managing robot programs, servers, and workpiece positions. Built with Spring Boot and PostgreSQL.

## Features

- **Program Management**: CRUD operations for robot program configurations
- **Workpiece Tracking**: Store and retrieve workpiece positions from vision system
- **Health Checks**: Built-in health endpoints via Spring Actuator
- **Docker Support**: Fully containerized with Docker Compose

## API Endpoints

### Programs

- `GET /api/programs` - Get all programs
- `GET /api/programs/{program_number}` - Get specific program by number
- `POST /api/programs` - Create new program
- `PUT /api/programs/{program_number}` - Update existing program
- `DELETE /api/programs/{program_number}` - Delete program

### Workpieces

- `POST /api/workpieces` - Create new workpiece position
- `GET /api/workpieces/latest` - Get latest workpiece position
- `GET /api/workpieces` - Get all workpiece positions
- `GET /api/workpieces/{id}` - Get specific workpiece by ID

### Health

- `GET /health` - Service health check

## Running with Docker Compose

### Prerequisites

- Docker
- Docker Compose

### Quick Start

From the repository root, run:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database on port 5432
- Config Service on port 8080

### Check Service Status

```bash
# Check if services are running
docker-compose ps

# Check service health
curl http://localhost:8080/health
```

### View Logs

```bash
# All services
docker-compose logs -f

# Config service only
docker-compose logs -f config-service

# PostgreSQL only
docker-compose logs -f postgres
```

### Stop Services

```bash
docker-compose down

# To also remove volumes (database data)
docker-compose down -v
```

## API Examples

### Create a Program

```bash
curl -X POST http://localhost:8080/api/programs \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: your-api-key" \
  -d '{
    "programNumber": 10,
    "programName": "Custom Program",
    "programType": "VISION",
    "description": "Custom vision program",
    "enabled": true
  }'
```

### Get All Programs

```bash
curl http://localhost:8080/api/programs
```

### Get Specific Program

```bash
curl http://localhost:8080/api/programs/1
```

### Create Workpiece Position

```bash
curl -X POST http://localhost:8080/api/workpieces \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: your-api-key" \
  -d '{
    "x": 300.0,
    "y": -320.0,
    "z": 200.0,
    "rx": -180.0,
    "ry": 0.0,
    "rz": 45.0,
    "score": 0.95,
    "sourceProgram": "GetNewWorkpiecePosition"
  }'
```

### Get Latest Workpiece

```bash
curl http://localhost:8080/api/workpieces/latest
```

## Environment Variables

The service can be configured using environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Service port | `8080` |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/robot_config` |
| `POSTGRES_USER` | Database username | `robot` |
| `POSTGRES_PASSWORD` | Database password | `robot123` |
| `LOG_LEVEL` | Application log level | `INFO` |
| `SHOW_SQL` | Show SQL queries in logs | `false` |

## Database Schema

### Programs Table

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGSERIAL | Primary key |
| `program_number` | INTEGER | Unique program number |
| `program_name` | VARCHAR(255) | Program name |
| `program_type` | VARCHAR(50) | ROBOT or VISION |
| `description` | TEXT | Program description |
| `enabled` | BOOLEAN | Whether program is enabled |
| `created_at` | TIMESTAMP | Creation timestamp |
| `updated_at` | TIMESTAMP | Last update timestamp |

### Workpiece Positions Table

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGSERIAL | Primary key |
| `x` | DOUBLE PRECISION | X coordinate |
| `y` | DOUBLE PRECISION | Y coordinate |
| `z` | DOUBLE PRECISION | Z coordinate |
| `rx` | DOUBLE PRECISION | Rotation X |
| `ry` | DOUBLE PRECISION | Rotation Y |
| `rz` | DOUBLE PRECISION | Rotation Z |
| `score` | DOUBLE PRECISION | Detection confidence score |
| `source_program` | VARCHAR(255) | Program that detected workpiece |
| `metadata` | TEXT | Additional metadata |
| `created_at` | TIMESTAMP | Detection timestamp |

## Development

### Build Locally

```bash
cd config-service
mvn clean package
```

### Run Locally (requires PostgreSQL)

```bash
# Set environment variables
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/robot_config
export POSTGRES_USER=robot
export POSTGRES_PASSWORD=robot123

# Run the application
java -jar target/config-service-1.0.0.jar
```

### Build Docker Image

```bash
cd config-service
docker build -t config-service:latest .
```

## Security

**Note:** API key authentication is planned but not enforced in this initial version. All endpoints accept an optional `X-API-KEY` header but do not validate it yet. This should be implemented before production deployment.

## Troubleshooting

### Connection Refused

If you get connection errors, ensure PostgreSQL is running:

```bash
docker-compose ps postgres
```

### Database Migration Errors

If Flyway migration fails, you may need to reset the database:

```bash
docker-compose down -v
docker-compose up -d
```

### Port Conflicts

If port 8080 is already in use, change the `SERVER_PORT` environment variable in `docker-compose.yml`.
