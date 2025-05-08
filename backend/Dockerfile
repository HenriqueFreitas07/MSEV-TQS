FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Package the application
RUN mvn package -DskipTests

# Use a smaller image to run the application
FROM openjdk:17

# Set working directory
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/backend-*.jar ./app.jar

# Verify the JAR exists
RUN ls -la
# Run the application
CMD ["java", "-jar", "app.jar"]