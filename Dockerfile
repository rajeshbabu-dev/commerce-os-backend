# =============================================================================
# CommerceOS Backend — Multi-stage Dockerfile
# =============================================================================
# Stage 1: Build the application with Maven
# Stage 2: Run the application with a slim JRE image
# =============================================================================

# -- Stage 1: Build -----------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build
COPY src/ ./src/
RUN ./mvnw package -DskipTests -B

# -- Stage 2: Run -------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Create non-root user for security
RUN groupadd -r commerceos && useradd -r -g commerceos commerceos

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/commerceos-backend-*.jar app.jar

# Create directory for logs
RUN mkdir -p /var/log/commerceos && chown -R commerceos:commerceos /var/log/commerceos

USER commerceos

EXPOSE 8080

# JVM options: reduce memory footprint for container
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:InitialRAMPercentage=50.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
