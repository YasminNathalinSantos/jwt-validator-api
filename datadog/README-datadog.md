# Observabilidade com Datadog

Este projeto já expõe tudo que o Datadog precisa para monitoramento completo,
através do Spring Boot Actuator + Micrometer. Este documento explica como
conectar a aplicação a uma conta real do Datadog.

## O que já está pronto na aplicação

- **Health check**: `GET /actuator/health`
- **Métricas padrão da JVM/Tomcat/HTTP**: `GET /actuator/metrics`
- **Métrica de negócio customizada**: `jwt.validation.count`, com tags:
  - `result`: `valid` ou `invalid`
  - `reason`: identificador curto da regra que falhou (ex: `name_rule`, `structure`, `none`)
  - `reason_description`: descrição legível do motivo (ex: "A claim Name nao pode conter numeros...")
- **Logging estruturado com correlação de requisição**: cada log inclui um
  `requestId` (via header `X-Request-Id`, gerado automaticamente se o cliente
  não enviar um)

## Passo a passo para conectar ao Datadog

### 1. Adicionar o Datadog Java Agent

Baixe o agent e rode a aplicação com ele anexado:

```bash
wget -O dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'

java -javaagent:dd-java-agent.jar \
     -Ddd.service=jwt-validator-api \
     -Ddd.env=production \
     -Ddd.logs.injection=true \
     -jar target/jwt-validator-api-0.0.1-SNAPSHOT.jar
```

Isso já habilita APM (tracing distribuído) automaticamente, sem alterar o código.

### 2. Habilitar o Micrometer Registry do Datadog (opcional, para métricas customizadas)

Adicionar no `pom.xml`:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-datadog</artifactId>
</dependency>
```

E configurar no `application.properties`:

```properties
management.datadog.metrics.export.api-key=${DATADOG_API_KEY}
management.datadog.metrics.export.enabled=true
```

Isso envia automaticamente a métrica `jwt.validation.count` e todas as
métricas do Actuator direto para o Datadog, sem precisar rodar o Agent
localmente (útil em ambientes serverless/containers).

### 3. Rodar o Datadog Agent (se optar por essa via em vez do Micrometer direto)

Ver `datadog-agent-values.yaml` neste mesmo diretório, com exemplo de
configuração para rodar via Docker ou Helm (Kubernetes).

### 4. Importar os monitors sugeridos

Os arquivos em `monitors/*.json` podem ser importados diretamente na conta
Datadog em **Monitors > New Monitor > Import from JSON**.

### 5. Importar o dashboard sugerido

O arquivo `dashboard/dashboard.json` pode ser importado em
**Dashboards > New Dashboard > Import Dashboard JSON**.

## Cenários de erro monitorados

| Cenário | Como é detectado | Monitor correspondente |
|---|---|---|
| Aplicação fora do ar | `/actuator/health` retorna `DOWN` ou não responde | `monitor-health-down.json` |
| Taxa de erro HTTP 5xx alta | Métrica `http.server.requests` com tag `status:5xx` | `monitor-alta-taxa-erro.json` |
| Latência alta (p95) | Métrica `http.server.requests` (percentil de tempo) | `monitor-latencia-alta.json` |
| Pico de JWTs inválidos | Métrica `jwt.validation.count` com tag `result:invalid` crescendo anormalmente (possível ataque/uso indevido/integração quebrada) | `monitor-pico-jwt-invalido.json` |

## Observação sobre a tag `reason_description`

Normalmente, usar descrições longas como valor de tag em métricas de alta
cardinalidade é desaconselhado (pode sobrecarregar o backend de métricas).
Neste projeto isso é seguro porque o conjunto de descrições é fixo e pequeno
(uma por regra de negócio, todas conhecidas em tempo de compilação), então
não há risco de explosão de cardinalidade.
