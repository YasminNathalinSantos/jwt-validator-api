package com.jwtvalidator.api.domain.rule;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.Set;


@Component
public class RoleRule implements ClaimValidationRule {

    private static final Set<String> ROLES_PERMITIDAS = Set.of("Admin", "Member", "External");

    @Override
    public boolean isValid(JsonNode payload) {
        JsonNode roleNode = payload.get("Role");
        if (roleNode == null || !roleNode.isTextual()) {
            return false;
        }
        return ROLES_PERMITIDAS.contains(roleNode.asText());
    }

    @Override
    public String name() {
        return "role_rule";
    }

    @Override
    public String failureReason() {
        return "A claim Role deve ser um dos valores: Admin, Member ou External";
    }
}
