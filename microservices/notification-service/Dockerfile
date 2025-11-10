FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/*.jar notificationservice.jar
ENTRYPOINT ["java", "-jar", "notificationservice.jar"]
