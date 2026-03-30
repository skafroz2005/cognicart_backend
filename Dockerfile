# Stage 1: Build the application using Eclipse Temurin Maven
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application, skipping tests to speed up deployment
RUN mvn clean package -DskipTests

# Stage 2: Run the application using Eclipse Temurin JRE (much lighter!)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built .jar file from the first stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]