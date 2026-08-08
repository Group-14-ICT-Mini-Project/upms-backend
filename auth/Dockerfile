FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY auth/mvnw auth/pom.xml ./auth/
COPY auth/.mvn ./auth/.mvn
COPY auth/src ./auth/src

WORKDIR /workspace/auth

RUN chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=render

COPY --from=build /workspace/auth/target/auth-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]