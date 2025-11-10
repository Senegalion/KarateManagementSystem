FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/*.jar authservice.jar
ENTRYPOINT ["java", "-jar", "authservice.jar"]
