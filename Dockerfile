# Multi-stage build for optimized image
FROM openjdk:17-jdk-slim as builder

WORKDIR /app

# Copy gradle wrapper and dependencies
COPY gradlew .
COPY gradle gradle/
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src/

# Build application
RUN ./gradlew clean build -x test --no-daemon

# Production image
FROM openjdk:17-jre-slim

WORKDIR /app

# Install necessary packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create non-root user for security
RUN addgroup --system spring && adduser --system --group spring
USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]