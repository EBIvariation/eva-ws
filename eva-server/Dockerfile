# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the full project
COPY . .
RUN mvn dependency:go-offline -B

# Build only eva-server and its required dependencies
RUN mvn -pl eva-server -am clean package -DskipTests -Dmaven.gitcommitid.skip=true

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r eva_server && useradd -r -g eva_server eva_server

# Create config directory for runtime application.properties mount
RUN mkdir -p /app/config && chown -R eva_server:eva_server /app

# Copy the JAR file from build stage
COPY --from=build /app/eva-server/target/*.jar app.jar

# Set ownership
RUN chown -R eva_server:eva_server /app

USER eva_server

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/eva/health || exit 1

# JVM options for containerized environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]