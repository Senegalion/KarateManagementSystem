FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/*.jar userservice.jar
ENTRYPOINT ["java", "-jar", "userservice.jar"]
