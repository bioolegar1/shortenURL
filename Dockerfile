# --- Estágio 1: Build (O Construtor) ---
# CORREÇÃO: Alterado de '17' para '21' para corresponder ao pom.xml
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Define o diretório de trabalho dentro do container.
WORKDIR /app

# Copia o pom.xml primeiro para aproveitar o cache de dependências do Docker.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o resto do código fonte.
COPY src ./src

# Executa o build do Maven para gerar o arquivo .jar.
RUN mvn clean package -DskipTests


# --- Estágio 2: Runtime (A Imagem Final) ---
# CORREÇÃO: Alterado de '17-jre-jammy' para '21-jre-jammy' para rodar a aplicação.
FROM eclipse-temurin:21-jre-jammy

# Define o diretório de trabalho.
WORKDIR /app

# Copia APENAS o .jar gerado do estágio 'builder' para a nossa imagem final.
COPY --from=builder /app/target/encurtalink-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta em que a aplicação Spring Boot roda por padrão.
EXPOSE 8080

# Comando para iniciar a aplicação quando o container for executado.
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]