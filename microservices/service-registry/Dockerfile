FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/service-registry-0.0.1-SNAPSHOT.jar ./service-registry.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar service-registry.jar"]
