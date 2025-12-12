FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/*.jar notificationservice.jar
ENTRYPOINT ["java", "-jar", "notificationservice.jar"]
