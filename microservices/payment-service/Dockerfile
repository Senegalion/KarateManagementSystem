FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/*.jar paymentservice.jar
ENTRYPOINT ["java", "-jar", "paymentservice.jar"]
