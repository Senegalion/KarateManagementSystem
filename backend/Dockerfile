FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . /app
RUN chmod +x ./gradlew
RUN ./gradlew build -x test
CMD ["java", "-jar", "build/libs/karate-management-system.jar"]
