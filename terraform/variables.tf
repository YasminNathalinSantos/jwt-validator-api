variable "aws_region" {
  description = "Regiao AWS onde os recursos serao provisionados"
  type        = string
  default     = "us-east-1"
}

variable "ecr_repository_url" {
  description = "URL do repositorio ECR contendo a imagem Docker da aplicacao"
  type        = string
  default     = "REPLACE_WITH_ECR_REPOSITORY_URL"
}
