# ==========================================
# Etapa 1: Construcción (Empaqueta el código)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
COPY . /app
WORKDIR /app
# Compilamos la aplicación saltando las pruebas para mayor velocidad
RUN mvn clean package -DskipTests

# ==========================================
# Etapa 2: Ejecución (Enciende el servidor)
# ==========================================
FROM eclipse-temurin:21-jdk-jammy
EXPOSE 8080
# Copiamos el ejecutable generado en la etapa 1
COPY --from=build /app/target/*.jar app.jar

# Comando de arranque
ENTRYPOINT ["java", "-jar", "/app.jar"]
