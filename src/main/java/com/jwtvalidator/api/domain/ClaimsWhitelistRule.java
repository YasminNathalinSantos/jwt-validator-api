package com.jwtvalidator.api.domain;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Garante que o payload tenha EXATAMENTE as claims permitidas: Name, Role e Seed.
 * Nem a mais, nem a menos.
 */
@Component
public class ClaimsWhitelistRule implements ClaimValidationRule {

    private static final Set<String> CLAIMS_PERMITIDAS = Set.of("Name", "Role", "Seed");

    @Override
    public boolean isValid(JsonNode payload) {
        if (payload.size() != CLAIMS_PERMITIDAS.size()) {
            return false;
        }
        Set<String> nomesDasClaims = new HashSet<>();
        payload.fieldNames().forEachRemaining(nomesDasClaims::add);
        return nomesDasClaims.equals(CLAIMS_PERMITIDAS);
    }

    @Override
    public String name() {
        return "ClaimsWhitelistRule";
    }
}