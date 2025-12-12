FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/*.jar enrollmentservice.jar
ENTRYPOINT ["java", "-jar", "enrollmentservice.jar"]
