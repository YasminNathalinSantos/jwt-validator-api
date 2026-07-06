package com.jwtvalidator.api.domain;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Contrato para uma regra de validação de claims.
 * Cada regra nova = uma nova implementação, sem alterar o orquestrador (Open/Closed).
 */
public interface ClaimValidationRule {

    boolean isValid(JsonNode payload);

    /** Nome da regra, usado em logs para saber o motivo da falha. */
    String name();
}