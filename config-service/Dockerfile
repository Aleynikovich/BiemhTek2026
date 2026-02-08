# Multi-stage build for Config Service
FROM maven:3.8.6-openjdk-11-slim AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application (dependencies will be downloaded during build)
RUN mvn clean package -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

# Runtime stage - use Eclipse Temurin (maintained OpenJDK distribution)
FROM eclipse-temurin:11-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables with defaults
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/robot_config
ENV POSTGRES_USER=robot
ENV POSTGRES_PASSWORD=robot123
ENV SERVER_PORT=8080

# Install curl for health check
RUN apk add --no-cache curl

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
