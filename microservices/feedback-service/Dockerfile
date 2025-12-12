FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/*.jar feedbackservice.jar
ENTRYPOINT ["java", "-jar", "feedbackservice.jar"]
