FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/*.jar ./config-server.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar config-server.jar"]
