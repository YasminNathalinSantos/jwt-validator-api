# JWT Validator API

Esse projeto nasceu de um case técnico bem direto: receber um JWT e dizer se ele é válido ou não, seguindo um conjunto de regras de negócio específicas. Por trás dessa pergunta simples, procurei construir algo com a mesma seriedade que teria em um ambiente de produção real: código testado, arquitetura extensível, observabilidade, containerização e um pipeline de CI/CD rodando de verdade.

## API em produção (deploy ao vivo)

A API está hospedada no Render e pode ser testada publicamente, sem precisar rodar nada localmente:

- **Swagger (documentação interativa)**: https://jwt-validator-api.onrender.com/swagger-ui/index.html
- **Health check**: https://jwt-validator-api.onrender.com/actuator/health
- **Endpoint de validação**: `POST https://jwt-validator-api.onrender.com/api/v1/jwt/validate`

> O plano gratuito do Render "adormece" a aplicação após alguns minutos de inatividade. A primeira requisição depois de um período ocioso pode levar até 50 segundos para responder, enquanto o serviço "acorda". As requisições seguintes respondem normalmente.

## Regras de validação

Para um JWT ser considerado válido, ele precisa:

1. Ser um JWT estruturalmente válido (3 segmentos, Base64URL decodificável, JSON válido no header e no payload)
2. Conter **exatamente** 3 claims: `Name`, `Role` e `Seed` — nem mais, nem menos
3. A claim `Name` não pode conter dígitos, e deve ter no máximo 256 caracteres
4. A claim `Role` deve ser exatamente um dos valores: `Admin`, `Member` ou `External`
5. A claim `Seed` deve representar um número primo válido

## Stack utilizada

- **Java 17** + **Spring Boot 4.1.0**
- **Maven**, com wrapper (`mvnw`) — não é necessário ter o Maven instalado
- **JUnit 5** + **AssertJ** para os testes
- **Jackson 2.x**, fixado explicitamente (ver seção "Decisões técnicas")
- **Micrometer** para métricas customizadas
- **Springdoc OpenAPI** (Swagger)
- **Docker**, com build multi-stage
- **GitHub Actions** para CI/CD

## Como rodar o projeto

### Pré-requisitos

- Java 17 ou superior instalado
- Não é necessário ter o Maven instalado — o projeto usa o Maven Wrapper

### Rodando localmente

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

### Rodando os testes

```bash
./mvnw clean test
```

### Rodando via Docker

```bash
docker build -t jwt-validator-api .
docker run -p 8080:8080 jwt-validator-api
```

Ou, usando o Docker Compose:

```bash
docker-compose up --build
```

> O Dockerfile foi validado através do pipeline de CI/CD (GitHub Actions), que builda a imagem e testa o health check em um ambiente Linux limpo. Isso aconteceu porque, durante o desenvolvimento, a máquina local não tinha virtualização habilitada na BIOS, o que impediu a instalação do Docker Desktop. A imagem foi hospedada e testada com sucesso no Render, então sabemos que ela funciona de ponta a ponta. Ver o workflow em `.github/workflows/ci.yml` e o histórico de execuções na aba "Actions" do repositório.

## Documentação interativa (Swagger)

Com a aplicação rodando (local ou em produção), acesse:

```
http://localhost:8080/swagger-ui/index.html
```

Lá é possível testar todos os endpoints diretamente pelo navegador, sem precisar montar requisições manualmente.

## Endpoints

### `POST /api/v1/jwt/validate`

O endpoint principal do case: valida um JWT conforme as regras de negócio descritas acima.

**Request body:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...."
}
```

**Response body:**
```json
{
  "valid": true
}
```

### `GET /api/v1/jwt/decode?token=...`

Um endpoint bônus, que não fazia parte do escopo original do enunciado. Foi adicionado durante o desenvolvimento para facilitar a visualização manual do payload de um JWT, sem precisar recorrer a ferramentas externas de decodificação. Disponível pelo Swagger; não está incluído nas coleções Insomnia/Postman por ser um endpoint de apoio, não parte do contrato da API.

**Response body:**
```json
{
  "structurallyValid": true,
  "payload": {
    "Role": "Admin",
    "Seed": "7841",
    "Name": "Toninho Araujo"
  }
}
```

### `GET /actuator/health`

Health check padrão do Spring Boot Actuator.

### `GET /actuator/metrics/jwt.validation.count`

M�trica customizada que conta as validações realizadas, com tags `result` (`valid`/`invalid`) e `reason` (motivo específico da falha, quando aplicável). Detalhes de observabilidade e integração com Datadog estão em `datadog/README-datadog.md`.

## Testando a API

Há duas coleções prontas na pasta `collections/`:

- `jwt-validator-api-insomnia-collection.json` — para importar no Insomnia
- `jwt-validator-api-postman-collection.json` — para importar no Postman

Ambas cobrem os 4 casos de teste do enunciado, além do health check e da métrica customizada. Por padrão, elas apontam para `http://localhost:8080` — para testar contra a API em produção, basta trocar essa variável (`base_url`) para `https://jwt-validator-api.onrender.com`.

## Arquitetura

O projeto segue uma separação em camadas, priorizando baixo acoplamento e facilidade de extensão:

```
src/main/java/com/jwtvalidator/api/
├── domain/
│   ├── rule/            # Implementacoes de ClaimValidationRule (Name, Role, Seed, whitelist)
│   ├── util/            # Utilitarios sem estado (ex: checagem de numero primo)
│   ├── JwtStructuralValidator.java
│   └── JwtStructureResult.java
├── application/         # Orquestracao das regras (JwtValidationService)
├── api/
│   ├── controller/      # Controllers REST
│   └── dto/             # Objetos de request/response
└── infrastructure/
    ├── config/          # Configuracao do Swagger/OpenAPI
    └── logging/         # Filtro de correlacao de requisicoes (requestId)
```

A camada `domain` não conhece HTTP nem o Spring — são só regras de negócio puras, fáceis de testar isoladamente. Dentro dela, `rule/` reúne as implementações de `ClaimValidationRule` (uma classe por regra), e `util/` guarda utilitários sem estado, como a checagem de número primo, que dão suporte às regras sem serem regras em si.

### Extensibilidade das regras (Open/Closed Principle)

Cada regra de validação implementa a interface `ClaimValidationRule`, com os métodos `isValid(payload)`, `name()` e `failureReason()`.

O `JwtValidationService` recebe uma `List<ClaimValidationRule>` via injeção de dependência do Spring. Na prática, isso significa que **adicionar uma nova regra de validação não exige tocar em nenhum código existente** — basta criar uma nova classe em `domain/rule`, anotada com `@Component`, implementando a interface. O Spring a registra automaticamente na lista, e o `JwtValidationService` passa a aplicá-la sem saber nada sobre ela além do contrato da interface.

## Observabilidade

- **Logging estruturado**: cada requisição HTTP recebe um `requestId` (via header `X-Request-Id`, gerado automaticamente quando o cliente não envia um), que aparece em todos os logs daquela requisição — útil para rastrear o que aconteceu numa chamada específica em meio a um volume grande de tráfego
- **Métricas**: contagem de validações por resultado e motivo de falha, expostas via Actuator/Micrometer
- **Terreno pronto para Datadog**: a pasta `datadog/` traz um README de integração, um exemplo de configuração do Agent, 4 monitors sugeridos e 1 dashboard pronto para importar — o objetivo é que, no dia em que houver uma conta Datadog disponível, a conexão seja praticamente plug-and-play

## Infraestrutura como código (Terraform)

A pasta `terraform/` contém um esqueleto descrevendo como a aplicação poderia ser provisionada na AWS via ECS Fargate (VPC, subnets, cluster ECS, task definition, IAM role de execução, CloudWatch Logs). Esse código **não foi aplicado** em uma conta AWS real — a hospedagem efetiva foi feita no Render, por não haver uma conta AWS disponível durante o desenvolvimento deste case. Ele serve para demonstrar como a arquitetura de deploy seria estruturada em um cenário AWS real. Detalhes em `terraform/README.md`.

## Decisões técnicas e premissas assumidas

Ao longo do desenvolvimento, algumas decisões precisaram ser tomadas onde o enunciado não era explícito, ou onde o ambiente impôs alguma limitação. Documento cada uma aqui, com o raciocínio por trás:

1. **"JWT válido" foi interpretado como estruturalmente válido, não com assinatura verificada.** O enunciado não forneceu nenhuma chave/segredo para verificação de assinatura, então a validação estrutural (3 segmentos, Base64URL decodificável, JSON válido) foi considerada suficiente para atender ao requisito.

2. **Jackson 2.x foi fixado explicitamente no `pom.xml`.** O Spring Boot 4.1.0 traz por padrão o Jackson 3 (pacote `tools.jackson.*`), que quebra compatibilidade com a API clássica `com.fasterxml.jackson.*`. Como o projeto usa amplamente essa API clássica, a versão 2.x foi fixada para garantir compatibilidade sem reescrever toda a camada de manipulação de JSON.

3. **O endpoint `/decode` não fazia parte do escopo original.** Foi adicionado como um bônus para facilitar a visualização manual do payload de um JWT durante os testes, disponível via Swagger. Internamente, o payload (um `JsonNode` do Jackson) é convertido para um `Map<String, Object>` antes de ser serializado na resposta — sem essa conversão, o Jackson serializa métodos internos do `JsonNode` (como `isArray()`, `isBoolean()`) como se fossem campos, poluindo a resposta.

4. **A tag `reason_description` nas métricas contém texto descritivo.** Normalmente, valores de tag longos ou muito variáveis são desaconselhados em métricas de alta cardinalidade, pois podem sobrecarregar sistemas como Datadog ou Prometheus. Neste caso é seguro, porque o conjunto de descrições é fixo e pequeno — uma por regra de negócio, todas conhecidas em tempo de compilação.

5. **Os testes do Controller não usam `MockMvc`.** O Spring Boot 4.1.0, por ser uma versão muito recente, apresentou incompatibilidades de dependências transitivas relacionadas ao `AutoConfigureMockMvc`. Como alternativa, os testes instanciam o Controller diretamente com suas dependências reais (o Service e as regras de negócio), validando o mesmo comportamento sem depender de uma infraestrutura de teste ainda instável nessa versão do framework.

6. **O Docker não foi testado localmente.** Por uma limitação de virtualização na BIOS da máquina de desenvolvimento, não foi possível instalar o Docker Desktop localmente. A validação do Dockerfile e do comportamento do container aconteceu via pipeline de CI/CD (GitHub Actions), em um ambiente Linux limpo, e depois confirmada na prática pelo próprio deploy bem-sucedido no Render.

## CI/CD

O pipeline (`.github/workflows/ci.yml`) roda automaticamente a cada push ou pull request para a branch `main`:

1. **Testes automatizados**: `./mvnw clean test`
2. **Build da imagem Docker**: builda a imagem e valida o health check do container antes de considerar o pipeline como sucesso

## Estrutura do repositório

```
.
├── src/                        # Codigo-fonte da aplicacao
├── collections/                # Colecoes Insomnia e Postman para teste manual
├── datadog/                    # Documentacao e configs de observabilidade
├── terraform/                  # Esqueleto de IaC para deploy em AWS (nao aplicado)
├── .github/workflows/          # Pipeline de CI/CD
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```
