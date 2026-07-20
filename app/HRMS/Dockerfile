# STAGE 1: Build the application using Maven and JDK 17
FROM maven:3.9-eclipse-temurin-17 AS builder
LABEL stage=builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests

# STAGE 2: Create the runtime image using JRE 17
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# Create a dedicated non-root user/group and own the app directory —
# without this, the process runs as root inside the container, which is a
# real escalation risk if the app is ever compromised (Semgrep flagged this).
RUN addgroup --system spring && adduser --system --ingroup spring spring \
    && mkdir -p /app/uploads \
    && chown -R spring:spring /app

# Matches server.port=8081 in application.properties — the original Dockerfile
# said 8080, which didn't match where the app actually listens.
EXPOSE 8081

COPY --from=builder /app/target/*.jar app.jar
RUN chown spring:spring /app/app.jar

USER spring:spring

ENTRYPOINT ["java", "-jar", "app.jar"]