# ═══════════════════════════════════════════════════════════════════════════════
# PSEI Authority - Dockerfile
# ═══════════════════════════════════════════════════════════════════════════════
# Multi-stage build for optimized production image

# ─── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:11-jdk AS build

WORKDIR /app

# Copy Gradle wrapper
COPY gradlew .
COPY gradle/ gradle/

# Copy build files
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Download dependencies (cached layer)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew build --no-daemon -x test

# ─── Stage 2: Runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:11-jre AS runtime

# Add a non-root user for security
RUN groupadd -r psei && useradd -r -g psei psei

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/build/libs/*.jar psei-service.jar

# Create uploads directory
RUN mkdir -p /app/uploads && chown -R psei:psei /app

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Expose port
EXPOSE 8080

# Switch to non-root user
USER psei

# Run the application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", \
            "-jar", "psei-service.jar"]
