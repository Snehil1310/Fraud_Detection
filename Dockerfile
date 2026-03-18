# Stage 1: Build the application using Maven
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copy maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (this layer is cached)
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source code and build
COPY src src
# Skip tests during Docker build as they run in CI
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create a non-root user for security
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Copy the built jar from the builder stage
COPY --from=builder /app/target/fraud-detection-system-1.0.0-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Configure healthcheck
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD wget -q -O - http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-XX:+UseZGC", "-jar", "app.jar"]
