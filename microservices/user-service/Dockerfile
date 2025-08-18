FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/*.jar userservice.jar
ENTRYPOINT ["java", "-jar", "userservice.jar"]
