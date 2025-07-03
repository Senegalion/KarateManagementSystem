FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY target/userservice.jar userservice.jar
ENTRYPOINT ["java", "-jar", "userservice.jar"]
