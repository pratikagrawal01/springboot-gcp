# Use a lightweight Java 17 runtime image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the Spring Boot JAR file
#COPY target/my-spring-boot-app.jar my-spring-boot-app.jar

# Expose the port your application runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "my-spring-boot-app.jar"]