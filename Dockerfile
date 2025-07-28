# --- Estágio 1: Build (O Construtor) ---
# Usamos uma imagem completa do Maven com o JDK 17 para compilar nosso projeto.
# 'AS builder' dá um nome a este estágio.
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Define o diretório de trabalho dentro do container.
WORKDIR /app

# Copia o pom.xml primeiro para aproveitar o cache de dependências do Docker.
# Se o pom.xml não mudar, o Docker reutiliza as dependências já baixadas.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o resto do código fonte.
COPY src ./src

# Executa o build do Maven para gerar o arquivo .jar.
# -DskipTests pula os testes para um build mais rápido no ambiente de deploy.
RUN mvn clean package -DskipTests


# --- Estágio 2: Runtime (A Imagem Final) ---
# Usamos uma imagem base muito menor, contendo apenas o Java Runtime (JRE),
# que é tudo que precisamos para rodar a aplicação.
FROM eclipse-temurin:17-jre-jammy

# Define o diretório de trabalho.
WORKDIR /app

# Copia APENAS o .jar gerado do estágio 'builder' para a nossa imagem final.
# Isso resulta em uma imagem muito menor e mais segura.
COPY --from=builder /app/target/encurtalink-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta em que a aplicação Spring Boot roda por padrão.
EXPOSE 8080

# Comando para iniciar a aplicação quando o container for executado.
# Ativa o perfil 'prod' para usar as configurações de produção.
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]