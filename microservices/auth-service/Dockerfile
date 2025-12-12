FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/*.jar authservice.jar
ENTRYPOINT ["java", "-jar", "authservice.jar"]
