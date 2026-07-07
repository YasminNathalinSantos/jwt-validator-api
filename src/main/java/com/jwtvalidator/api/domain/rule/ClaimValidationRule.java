package com.jwtvalidator.api.domain.rule;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Contrato para uma regra de validação de claims.
 * Cada regra nova = uma nova implementação, sem alterar o orquestrador (Open/Closed).
 */
public interface ClaimValidationRule {

    boolean isValid(JsonNode payload);

    /**
     * Identificador curto e estável da regra, usado como valor de tag em
     * métricas (ex: Datadog/Prometheus). Deve ser sempre o mesmo texto,
     * sem espaços, para não gerar cardinalidade excessiva na métrica.
     */
    String name();

    /**
     * Descrição legível do motivo de falha, usada em logs para facilitar
     * o diagnóstico por humanos (ex: em um dashboard ou console de log).
     */
    String failureReason();
}
