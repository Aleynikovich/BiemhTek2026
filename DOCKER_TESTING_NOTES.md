# Docker Compose Testing Notes

## Testing Status

Due to certificate/network constraints in the CI environment, Docker Compose testing was not completed in this automated PR. 

## Manual Testing Required

To test the config service with Docker Compose:

### Prerequisites
- Docker and Docker Compose installed
- Internet access for pulling images
- Ports 5432 and 8080 available

### Steps

1. Navigate to repository root:
   ```bash
   cd /path/to/BiemhTek2026
   ```

2. Start services:
   ```bash
   docker compose up -d
   ```

3. Wait for services to be healthy (30-60 seconds):
   ```bash
   docker compose ps
   ```

4. Test health endpoint:
   ```bash
   curl http://localhost:8080/health
   ```

5. Test API endpoints:
   ```bash
   # Get all programs
   curl http://localhost:8080/api/programs
   
   # Get specific program
   curl http://localhost:8080/api/programs/1
   
   # Create workpiece position
   curl -X POST http://localhost:8080/api/workpieces \
     -H "Content-Type: application/json" \
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
   
   # Get latest workpiece
   curl http://localhost:8080/api/workpieces/latest
   ```

6. View logs:
   ```bash
   docker compose logs -f config-service
   ```

7. Stop services:
   ```bash
   docker compose down
   ```

### Alternative: Build Locally

If Docker Compose has issues, you can build and run locally with Maven:

1. Install PostgreSQL locally and create database:
   ```sql
   CREATE DATABASE robot_config;
   CREATE USER robot WITH PASSWORD 'robot123';
   GRANT ALL PRIVILEGES ON DATABASE robot_config TO robot;
   ```

2. Navigate to config-service:
   ```bash
   cd config-service
   ```

3. Build:
   ```bash
   mvn clean package
   ```

4. Run:
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/robot_config
   export POSTGRES_USER=robot
   export POSTGRES_PASSWORD=robot123
   java -jar target/config-service-1.0.0.jar
   ```

5. Test endpoints as above

## Expected Results

- Service starts without errors
- Health endpoint returns HTTP 200 with status UP
- 7 programs are pre-loaded (programs 1-7)
- All CRUD operations work correctly
- Flyway migrations execute successfully

## Known Issues

- Docker image pulls may fail if behind corporate proxy
- Certificate issues with Maven in Docker may require additional configuration
- Port conflicts if 5432 or 8080 already in use

## Troubleshooting

### Connection Refused
- Ensure PostgreSQL is running: `docker compose ps postgres`
- Check service logs: `docker compose logs postgres`

### Port Already in Use
- Change ports in docker-compose.yml:
  ```yaml
  ports:
    - "15432:5432"  # PostgreSQL
    - "18080:8080"  # Config Service
  ```

### Build Failures
- Clear Docker cache: `docker system prune -a`
- Try building without cache: `docker compose build --no-cache`

## Verification Checklist

- [ ] Services start successfully
- [ ] Health endpoint accessible
- [ ] GET /api/programs returns 7 programs
- [ ] GET /api/programs/1 returns GetNewWorkpiecePosition
- [ ] POST /api/workpieces creates position
- [ ] GET /api/workpieces/latest retrieves position
- [ ] Database persists data across restarts
- [ ] Flyway migrations complete successfully
