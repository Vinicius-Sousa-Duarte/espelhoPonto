# 1. Imagem base com Java 21 (Leve e otimizada)
FROM eclipse-temurin:21-jdk-alpine

# 2. Define o diretório de trabalho dentro do container
WORKDIR /app

# 3. Cria um usuário não-root por segurança (Best Practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 4. Copia o JAR gerado para dentro do container
# O asterisco garante que pegue qualquer versão (0.0.1, 1.0.0...)
COPY target/*.jar app.jar

# 5. Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]