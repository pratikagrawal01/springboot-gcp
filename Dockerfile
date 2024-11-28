# Use Java 17 JDK as base image
FROM openjdk:17-jdk-slim

# Set application work directory
WORKDIR /app

# Copy JAR file into the container
COPY target/gcp-0.0.1-SNAPSHOT.jar gcp-0.0.1-SNAPSHOT.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "gcp-0.0.1-SNAPSHOT.jar"]