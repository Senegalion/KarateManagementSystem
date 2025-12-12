FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/*.jar trainingservice.jar
ENTRYPOINT ["java", "-jar", "trainingservice.jar"]
