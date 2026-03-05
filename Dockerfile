FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /build/target/*.jar app.jar

# Inicia a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]