package com.jwtvalidator.api.domain;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/**
 * A claim Name não pode conter dígitos e deve ter no máximo 256 caracteres.
 */
@Component
public class NameRule implements ClaimValidationRule {

    private static final int TAMANHO_MAXIMO = 256;

    @Override
    public boolean isValid(JsonNode payload) {
        JsonNode nameNode = payload.get("Name");
        if (nameNode == null || !nameNode.isTextual()) {
            return false;
        }
        String name = nameNode.asText();
        if (name.isEmpty() || name.length() > TAMANHO_MAXIMO) {
            return false;
        }
        return name.chars().noneMatch(Character::isDigit);
    }

    @Override
    public String name() {
        return "name_rule";
    }

    @Override
    public String failureReason() {
        return "A claim Name nao pode conter numeros e deve ter no maximo 256 caracteres";
    }
}
