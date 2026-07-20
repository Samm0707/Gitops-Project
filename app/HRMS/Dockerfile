# STAGE 1: Build the application using Maven and JDK 17
FROM maven:3.9-eclipse-temurin-17 AS builder
LABEL stage=builder

# Set the working directory in the container
WORKDIR /app

# Copy the Maven wrapper files and pom.xml
# This is done first to leverage Docker's layer caching for dependencies.
# If pom.xml and .mvn haven't changed, Docker can reuse the dependency download layer.
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# --- ADDED: Make the Maven wrapper script executable ---
RUN chmod +x mvnw
# -----------------------------------------------------

# Download dependencies. Using go-offline first can speed up subsequent builds if pom.xml hasn't changed.
# -B runs in batch mode (non-interactive)
RUN ./mvnw dependency:go-offline -B

# Copy the rest of your application's source code
COPY src ./src

# Package the application (this will create the JAR).
# -DskipTests will skip running tests during the Docker build, which is often desired.
# Remove -DskipTests if you want to run tests during the image build.
RUN ./mvnw package -DskipTests

# STAGE 2: Create the runtime image using JRE 17
FROM eclipse-temurin:17-jre-focal

# Set the working directory in the container
WORKDIR /app

# Expose the port your Spring Boot application runs on (from server.port in application.properties)
EXPOSE 8080

# Copy the JAR file from the 'builder' stage to the current stage
# Maven typically puts the JAR in /app/target/your-app-name.jar
COPY --from=builder /app/target/*.jar app.jar

# Command to run the application when the container starts
# Your application.properties will be inside app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
