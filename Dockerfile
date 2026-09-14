FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY target/EventHub-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]