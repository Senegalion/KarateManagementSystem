FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/*.jar paymentservice.jar
ENTRYPOINT ["java", "-jar", "paymentservice.jar"]
