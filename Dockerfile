# Stage 1: Build the application using Maven
FROM maven:3.8.5-openjdk-17 as build

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY src ./src

# Run Maven package to build the application
RUN mvn clean package -DskipTests

# Stage 2: Package the application in a smaller runtime image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar my-spring-boot-app.jar

# Expose the application port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "my-spring-boot-app.jar"]

