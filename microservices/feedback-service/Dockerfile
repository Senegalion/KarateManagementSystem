FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/*.jar feedbackservice.jar
ENTRYPOINT ["java", "-jar", "feedbackservice.jar"]
