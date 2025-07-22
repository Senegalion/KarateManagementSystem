FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/*.jar ./api-gateway.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar api-gateway.jar"]
