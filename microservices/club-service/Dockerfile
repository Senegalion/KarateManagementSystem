FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/*.jar clubservice.jar
ENTRYPOINT ["java", "-jar", "clubservice.jar"]
