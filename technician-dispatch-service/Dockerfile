# ---------- Etapa 1: build (Maven + JDK) ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
# Primero solo el pom: así Docker cachea las dependencias si el código cambia
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# ---------- Etapa 2: run (solo JRE, imagen liviana) ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
