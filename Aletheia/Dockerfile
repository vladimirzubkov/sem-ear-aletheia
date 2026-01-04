# 1. Use a lightweight base image with Java 17 (Alpine Linux)
FROM eclipse-temurin:17-jre-alpine

# 2. Add metadata about the maintainer (optional)
LABEL maintainer="cvut-student"

# 3. Create a temporary volume (useful for Tomcat/Spring Boot temporary files)
VOLUME /tmp

# 4. Copy the built JAR file from the target directory into the container
# Note: This assumes you have already run 'mvn package'
COPY target/*.jar app.jar

# 5. Expose port 8080 to the outside world
EXPOSE 8080

# 6. Command to run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]