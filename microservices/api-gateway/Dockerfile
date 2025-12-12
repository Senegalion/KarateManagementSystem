FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/*.jar ./api-gateway.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar api-gateway.jar"]
