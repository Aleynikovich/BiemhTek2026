# Config Service

REST API service for managing KUKA robot program configurations and workpiece positions.

## Features

- **Program Management**: Define and manage robot programs with type classification (ROBOT/VISION)
- **Workpiece Position Tracking**: Store and retrieve workpiece positions from the vision system
- **PostgreSQL Backend**: Persistent storage with Flyway migrations
- **Health Monitoring**: Built-in health check endpoint via Spring Boot Actuator

## Prerequisites

- Docker and Docker Compose
- Java 11+ (for local development without Docker)
- Maven 3.6+ (for local development without Docker)

## Quick Start with Docker Compose

From the repository root:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database on port 5432
- Config Service API on port 8080

## API Endpoints

### Programs

- `GET /api/programs` - Get all programs
- `GET /api/programs/{programNumber}` - Get program by number
- `POST /api/programs` - Create new program
- `PUT /api/programs/{programNumber}` - Update program
- `DELETE /api/programs/{programNumber}` - Delete program

### Workpieces

- `POST /api/workpieces` - Create new workpiece position
- `GET /api/workpieces/latest` - Get most recent workpiece position
- `GET /api/workpieces` - Get all workpiece positions (default limit: 100)
- `GET /api/workpieces/{id}` - Get specific workpiece position

### Health

- `GET /health` - Application health status
- `GET /actuator/health` - Detailed health information

## Example API Calls

### Create a Program

```bash
curl -X POST http://localhost:8080/api/programs \
  -H "Content-Type: application/json" \
  -d '{
    "programNumber": 10,
    "programName": "Custom Pick",
    "programType": "ROBOT",
    "description": "Custom pick operation",
    "enabled": true
  }'
```

### Get Program by Number

```bash
curl http://localhost:8080/api/programs/1
```

### Create Workpiece Position

```bash
curl -X POST http://localhost:8080/api/workpieces \
  -H "Content-Type: application/json" \
  -d '{
    "x": 300.5,
    "y": -320.0,
    "z": 200.0,
    "rx": -180.0,
    "ry": 0.0,
    "rz": 45.0,
    "score": 0.95,
    "sourceProgram": "GetNewWorkpiecePosition",
    "metadata": ""
  }'
```

### Get Latest Workpiece Position

```bash
curl http://localhost:8080/api/workpieces/latest
```

## Local Development (without Docker)

### Prerequisites

1. PostgreSQL running locally on port 5432
2. Database named `robot_config` created

### Run the Application

```bash
cd config-service
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean package
java -jar target/config-service-1.0.0.jar
```

## Environment Variables

- `DATABASE_URL` - JDBC URL for PostgreSQL (default: `jdbc:postgresql://localhost:5432/robot_config`)
- `DATABASE_USER` - Database username (default: `postgres`)
- `DATABASE_PASSWORD` - Database password (default: `postgres`)
- `SERVER_PORT` - Server port (default: `8080`)

## Database Schema

The database is initialized with Flyway migration `V1__init.sql` which creates:

- `programs` table: Stores program configurations
- `workpiece_positions` table: Stores workpiece positions from vision system
- Sample programs (1-7) matching the existing robot programs

## Security Note

The current implementation includes a TODO placeholder for API key authentication via the `X-API-KEY` header. This is not enforced in this initial version but is marked for future implementation in production environments.

## Architecture

- **Spring Boot 2.7.18**: Provides REST framework
- **Spring Data JPA**: Database access layer
- **PostgreSQL**: Relational database
- **Flyway**: Database migration management
- **Lombok**: Reduces boilerplate code
- **Spring Boot Actuator**: Health checks and monitoring
