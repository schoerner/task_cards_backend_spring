FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

#RUN mvn -B -DskipTests package
RUN mvn -B -DskipTests package

FROM openjdk:24-jdk-slim
WORKDIR /app

COPY --from=build /app/target/*SNAPSHOT.jar app.jar

EXPOSE 8088

CMD ["java", "-jar", "app.jar"]