FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/*.jar enrollmentservice.jar
ENTRYPOINT ["java", "-jar", "enrollmentservice.jar"]
