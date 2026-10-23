
FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --chown=10001:10001 target/demo-0.0.1-SNAPSHOT.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
