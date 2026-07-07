# Terraform - Infraestrutura AWS (ECS Fargate)

Este diretorio contem um esqueleto de Terraform ilustrando como a jwt-validator-api
poderia ser provisionada em producao na AWS, usando ECS Fargate.

## Importante

Este codigo NAO foi aplicado (terraform apply) em uma conta AWS real. A hospedagem
efetiva do projeto foi feita no Render (ver README principal), devido a nao haver
uma conta AWS disponivel no momento do desenvolvimento deste case.

O objetivo deste Terraform e demonstrar conhecimento de Infraestrutura como Codigo
e como a arquitetura de deploy seria estruturada em um ambiente AWS real.

## Recursos provisionados

- VPC com 2 subnets publicas em AZs distintas
- Internet Gateway e roteamento publico
- Cluster ECS (Fargate)
- Task Definition com health check configurado
- ECS Service com IP publico
- Security Group liberando a porta 8080
- CloudWatch Log Group para logs da aplicacao
- IAM Role de execucao da task

## Pre-requisitos para uso real

1. Uma imagem Docker da aplicacao publicada em um repositorio ECR
2. Credenciais AWS configuradas (aws configure ou variaveis de ambiente)
3. Terraform >= 1.5.0 instalado

## Como seria usado

terraform init
terraform plan -var="ecr_repository_url=<url-do-seu-ecr>"
terraform apply -var="ecr_repository_url=<url-do-seu-ecr>"

## Limitacoes conhecidas

- Nao inclui Application Load Balancer (em producao real, seria recomendado
  para permitir HTTPS e multiplas instancias com balanceamento)
- Nao inclui auto-scaling
- Nao inclui banco de dados ou armazenamento persistente (nao ha necessidade
  neste projeto, ja que a aplicacao e stateless)
