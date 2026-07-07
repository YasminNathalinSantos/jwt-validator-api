# JWT Validator API

API REST que recebe um JWT (string) e verifica se ele e valido conforme regras de negocio customizadas, retornando um booleano (true/false).

## API em producao (deploy ao vivo)

A API esta hospedada no Render e pode ser testada publicamente, sem precisar rodar nada localmente:

- **Swagger (documentacao interativa)**: https://jwt-validator-api.onrender.com/swagger-ui/index.html
- **Health check**: https://jwt-validator-api.onrender.com/actuator/health
- **Endpoint de validacao**: POST https://jwt-validator-api.onrender.com/api/v1/jwt/validate

> Nota: o plano gratuito do Render "adormece" a aplicacao apos alguns minutos de inatividade. A primeira requisicao apos um periodo ocioso pode levar ate 50 segundos para responder, enquanto o servico "acorda". Requisicoes seguintes respondem normalmente.

## Regras de validacao

Para um JWT ser considerado valido, ele precisa:

1. Ser um JWT estruturalmente valido (3 segmentos, Base64URL decodificavel, JSON valido no header e payload)
2. Conter exatamente 3 claims: Name, Role e Seed (nem mais, nem menos)
3. A claim Name nao pode conter digitos, e deve ter no maximo 256 caracteres
4. A claim Role deve ser exatamente um dos valores: Admin, Member ou External
5. A claim Seed deve representar um numero primo valido

## Stack utilizada

- Java 17 + Spring Boot 4.1.0
- Maven (com wrapper mvnw, nao precisa ter o Maven instalado)
- JUnit 5 + AssertJ para testes
- Jackson 2.x (fixado explicitamente - ver secao "Decisoes tecnicas")
- Micrometer para metricas customizadas
- Springdoc OpenAPI (Swagger)
- Docker (multi-stage build)
- GitHub Actions para CI/CD

## Como rodar o projeto

### Pre-requisitos
- Java 17 ou superior instalado
- Nao e necessario ter o Maven instalado (o projeto usa o Maven Wrapper)

### Rodando localmente

./mvnw spring-boot:run

A aplicacao sobe em http://localhost:8080

### Rodando os testes

./mvnw clean test

### Rodando via Docker

docker build -t jwt-validator-api .
docker run -p 8080:8080 jwt-validator-api

Ou, usando o Docker Compose:

docker-compose up --build

Nota: o Dockerfile foi validado atraves do pipeline de CI/CD (GitHub Actions), que builda a imagem e testa o health check em um ambiente Linux limpo. Isso ocorreu devido a uma limitacao de virtualizacao (BIOS) na maquina de desenvolvimento local, impedindo a instalacao do Docker Desktop localmente durante o desenvolvimento. Ver o workflow em .github/workflows/ci.yml e o historico de execucoes na aba "Actions" do repositorio.

## Documentacao interativa (Swagger)

Com a aplicacao rodando, acesse:

http://localhost:8080/swagger-ui/index.html

La e possivel testar os endpoints diretamente pelo navegador.

## Endpoints

### POST /api/v1/jwt/validate

Valida um JWT conforme as regras de negocio descritas acima.

Request body:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...."
}

Response body:
{
  "valid": true
}

### GET /api/v1/jwt/decode?token=...

Endpoint auxiliar de debug/visualizacao: decodifica e exibe o payload de um JWT, sem aplicar as regras de negocio. Nao faz parte do escopo original do enunciado - foi adicionado como conveniencia para facilitar testes manuais durante o desenvolvimento. Disponivel apenas via Swagger (nao incluido nas colecoes Insomnia/Postman).

Response body:
{
  "structurallyValid": true,
  "payload": {
    "Role": "Admin",
    "Seed": "7841",
    "Name": "Toninho Araujo"
  }
}

### GET /actuator/health

Health check da aplicacao.

### GET /actuator/metrics/jwt.validation.count

Metrica customizada contando validacoes, com tags result (valid/invalid) e reason (motivo especifico da falha). Ver datadog/README-datadog.md para detalhes de observabilidade.

## Testando a API

Duas colecoes prontas estao disponiveis na pasta collections/:

- jwt-validator-api-insomnia-collection.json (importar no Insomnia)
- jwt-validator-api-postman-collection.json (importar no Postman)

Ambas incluem os 4 casos de teste descritos no enunciado, alem do health check e da metrica customizada.

## Arquitetura

O projeto segue uma separacao em camadas, com enfase em baixo acoplamento e extensibilidade:

src/main/java/com/jwtvalidator/api/
- domain/           Regras de negocio puras (nao conhecem HTTP nem Spring)
- application/      Orquestracao das regras (JwtValidationService)
- api/              Controllers e DTOs (traducao HTTP <-> Service)
- infrastructure/   Configuracoes transversais (logging, OpenAPI)

### Extensibilidade das regras (Open/Closed Principle)

Cada regra de validacao implementa a interface ClaimValidationRule, com os metodos isValid(payload), name() e failureReason().

O JwtValidationService recebe uma List<ClaimValidationRule> via injecao de dependencia do Spring. Isso significa que adicionar uma nova regra de validacao nao exige alterar nenhum codigo existente - basta criar uma nova classe anotada com @Component que implemente a interface, e o Spring a registra automaticamente na lista.

## Observabilidade

- Logging estruturado: cada requisicao HTTP recebe um requestId (via header X-Request-Id, gerado automaticamente se o cliente nao enviar), que aparece em todos os logs daquela requisicao
- Metricas: contagem de validacoes por resultado e motivo de falha, expostas via Actuator/Micrometer
- Terreno pronto para Datadog: ver pasta datadog/, com README de integracao, exemplo de config do Agent, 4 monitors sugeridos e 1 dashboard pronto para importar

## Decisoes tecnicas e premissas assumidas

1. "JWT valido" foi interpretado como estruturalmente valido, nao com assinatura verificada. O enunciado nao forneceu nenhuma chave/segredo para verificacao de assinatura, entao a validacao estrutural (3 segmentos, Base64URL decodificavel, JSON valido) foi considerada suficiente para atender ao requisito "deve ser um JWT valido".

2. Jackson 2.x foi fixado explicitamente no pom.xml. O Spring Boot 4.1.0 traz por padrao o Jackson 3 (pacote tools.jackson.*), que quebra compatibilidade com a API classica com.fasterxml.jackson.*. Como o projeto usa amplamente essa API classica, a versao 2.x foi fixada para garantir compatibilidade sem reescrever toda a camada de manipulacao de JSON.

3. O endpoint /decode nao fazia parte do escopo original. Foi adicionado como um endpoint bonus para facilitar a visualizacao manual do payload de um JWT durante testes, disponivel via Swagger.

4. A tag reason_description nas metricas contem texto descritivo. Normalmente, valores de tag longos/variaveis sao desaconselhados em metricas de alta cardinalidade (podem sobrecarregar sistemas como Datadog/Prometheus). Neste caso e seguro, pois o conjunto de descricoes e fixo e pequeno (uma por regra de negocio, conhecidas em tempo de compilacao).

5. Testes do Controller nao usam MockMvc. O Spring Boot 4.1.0, por ser uma versao muito recente, apresentou incompatibilidades de dependencias transitivas relacionadas ao AutoConfigureMockMvc. Como alternativa, os testes instanciam o Controller diretamente com suas dependencias reais (Service e regras de negocio), validando o mesmo comportamento sem depender de infraestrutura de teste ainda instavel nessa versao do framework.

6. Docker nao foi testado localmente. Devido a uma limitacao de virtualizacao (BIOS) na maquina de desenvolvimento, o Docker Desktop nao pode ser instalado localmente. A validacao do Dockerfile e do comportamento do container ocorre via pipeline de CI/CD (GitHub Actions), em ambiente Linux limpo - ver .github/workflows/ci.yml.

## CI/CD

O pipeline (.github/workflows/ci.yml) roda automaticamente a cada push/PR para a branch main:

1. Testes automatizados: ./mvnw clean test
2. Build da imagem Docker: builda a imagem e valida o health check do container

## Estrutura do repositorio

.
- src/                        Codigo-fonte da aplicacao
- collections/                Colecoes Insomnia e Postman para teste manual
- datadog/                    Documentacao e configs de observabilidade
- .github/workflows/          Pipeline de CI/CD
- Dockerfile
- docker-compose.yml
- pom.xml
