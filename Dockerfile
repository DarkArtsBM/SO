# -----------------------------------------------------------------
# FASE 1: "BUILDER" - Compilar o projeto Java com Maven e JDK 21
# -----------------------------------------------------------------
# Usamos uma imagem oficial que contém o Maven e o JDK 21 (baseado no seu pom.xml)
FROM maven:3.9.6-eclipse-temurin-21-jammy AS builder

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia apenas os ficheiros de definição do Maven
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Descarrega todas as dependências do pom.xml
# Isto cria uma "camada" no Docker que só é executada se o pom.xml mudar,
# tornando builds futuros muito mais rápidos.
RUN ./mvnw dependency:go-offline

# Agora, copia o resto do código-fonte (a pasta 'src')
COPY src ./src

# Compila o projeto e empacota tudo num ficheiro .jar
# O frontend (em src/main/resources/static) será incluído dentro do .jar
# -DskipTests acelera o build (não executa os testes)
RUN ./mvnw package -DskipTests

# -----------------------------------------------------------------
# FASE 2: "RUNNER" - Criar a imagem final e leve para execução
# -----------------------------------------------------------------
# Usamos uma imagem JRE (Java Runtime Environment) leve.
# Ela é muito mais pequena porque não tem o compilador (JDK) ou o Maven.
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copia APENAS o ficheiro .jar final da fase 'builder'
# (O nome do .jar vem do seu pom.xml: <artifactId>cloudjrb</artifactId>-<version>0.0.1-SNAPSHOT</version>)
COPY --from=builder /app/target/cloudjrb-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta 8080 (a porta que o Spring Boot usa)
EXPOSE 8080

# Comando para executar a aplicação quando o contêiner iniciar
# Este comando cria os ficheiros na pasta /app/data (relativo ao WORKDIR)
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]