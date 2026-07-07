# ---------- Etapa 1: build ----------
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copia primeiro o wrapper e o pom.xml para aproveitar o cache de dependencias
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Baixa as dependencias em uma camada separada (cache do Docker)
RUN ./mvnw dependency:go-offline -B

# Agora copia o codigo-fonte e builda
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Cria um usuario nao-root por seguranca
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
