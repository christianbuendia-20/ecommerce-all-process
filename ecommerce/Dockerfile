FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/ecommerce-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "app.jar"]
