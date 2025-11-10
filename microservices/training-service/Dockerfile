FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/*.jar trainingservice.jar
ENTRYPOINT ["java", "-jar", "trainingservice.jar"]
