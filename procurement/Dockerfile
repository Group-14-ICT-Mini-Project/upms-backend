FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY procurement/mvnw procurement/pom.xml ./procurement/
COPY procurement/.mvn ./procurement/.mvn
COPY procurement/src ./procurement/src

WORKDIR /workspace/procurement

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=render

COPY --from=build /workspace/procurement/target/procurement-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/app.jar"]