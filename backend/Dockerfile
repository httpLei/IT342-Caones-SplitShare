FROM maven:3.9.9-eclipse-temurin-17 AS build

# Set the working execution context folder inside the container
WORKDIR /app

# Copy the dependency tracking configuration file first to leverage Docker caching layers
COPY pom.xml .

# Pre-download system dependencies to speed up future incremental builds
RUN mvn dependency:go-offline -B

# Copy your source code directory into the compilation container workspace
COPY src ./src

# Compile and package your production binary artifact JAR file while skipping testing suites
RUN mvn clean package -DskipTests

# =========================================================================
# STAGE 2: Runtime Environment (Lightweight, High-Performance Execution)
# =========================================================================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the generated Spring Boot jar into the runtime image
COPY --from=build /app/target/*.jar application.jar

# Open standard networking port 8080 inside the container bridge network layer
EXPOSE 8080

# Launch the executable Spring Boot package
ENTRYPOINT ["java", "-jar", "application.jar"]
