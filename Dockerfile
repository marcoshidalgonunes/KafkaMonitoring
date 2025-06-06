# Use Maven to build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Use a lightweight JRE for running the app
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose port if your app listens on one (e.g., 8080)
EXPOSE 8080

# Set environment
ENV JAVA_TOOL_OPTIONS="\
  -Djava.util.logging.ConsoleHandler.level=FINEST \
  -Dspring.profiles.active=docker"
  
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]