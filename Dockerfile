# Build-Phase mit Maven und Java 21
FROM maven:3.9.14-eclipse-temurin-21 AS build

# Setzt das Arbeitsverzeichnis im Container
WORKDIR /app

# Kopiert nur die pom.xml zuerst, um Dependencies zu cachen
COPY pom.xml .

# Lädt Maven-Dependencies (wird gecached, solange pom.xml gleich bleibt)
RUN mvn -B -DskipTests package

# Kopiert den Quellcode ins Image
COPY src ./src

# Führt den Maven-Build aus (ohne Tests)
RUN mvn -B -DskipTests package

# Laufzeit-Phase mit passender Java-Version (21)
FROM eclipse-temurin:21-jre-jammy

# Setzt das Arbeitsverzeichnis im Container
WORKDIR /app

# Kopiert das gebaute JAR aus dem vorherigen Build-Container
COPY --from=build /app/target/*.jar app.jar

# Gibt den Port frei, den die App verwendet
EXPOSE 8080

# Startbefehl für die Java-Anwendung
CMD ["java", "-jar", "app.jar"]
