# Multi-stage build for Spring Boot application
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./

# Make gradlew executable (Fix for permission denied error)
RUN chmod +x ./gradlew

# Copy source code
COPY src src

# Build application (skip tests for faster build)
RUN ./gradlew clean build -x test

# Production stage
FROM eclipse-temurin:17-jre-alpine

# Create non-root user for security
RUN adduser -D -u 1001 appuser

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder --chown=appuser:appuser /app/build/libs/Cal-Todo-CRUD-0.0.1-SNAPSHOT.jar app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]