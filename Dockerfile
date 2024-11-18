# Etapa de Build
FROM maven:3.8.5-eclipse-temurin-17 as build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa de Package
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/energyapi-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]