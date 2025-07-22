FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/*.jar clubservice.jar
ENTRYPOINT ["java", "-jar", "clubservice.jar"]
