# ======================================================================
# HackathonBuddy — Backend Production Dockerfile (Repository Root context)
# For Render services configured with Root Directory as "."
# ======================================================================

# ─── Stage 1: Build JAR with Maven & OpenJDK 17 ────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache Maven dependencies
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

# Copy backend source code and compile
COPY backend/src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2: Minimal & Secure Production Runtime ──────────────────────
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Security: Run as dedicated non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Copy compiled JAR from build stage
COPY --from=builder /app/target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring

# Render assigns dynamic port via $PORT. Default to 8081 if unset.
ENV PORT=8081
EXPOSE 8081

# JVM flags:
# - UseContainerSupport: respects cgroup memory limits (crucial for Render 512MB RAM)
# - MaxRAMPercentage=75.0: caps heap at 75% container memory to prevent OOM
# - Dserver.port: dynamically binds Spring Boot to Render's $PORT
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT} -jar app.jar"]

