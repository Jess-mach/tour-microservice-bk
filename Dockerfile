FROM openjdk:21-jdk-slim

# Instalar dependências necessárias
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Criar diretório da aplicação
WORKDIR /app

# Copiar arquivos de configuração do Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Dar permissão de execução para o Maven wrapper
RUN chmod +x mvnw

# Baixar dependências
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src src

# Build da aplicação
RUN ./mvnw clean package -DskipTests

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

# Comando para iniciar a aplicação com otimizações para JDK 21
ENTRYPOINT ["java", \
    "--enable-preview", \
    "-XX:+UseZGC", \
    "-XX:+UnlockExperimentalVMOptions", \
    "-Xms512m", \
    "-Xmx1024m", \
    "-jar", "target/tourapp-backend-1.0.0.jar"]

