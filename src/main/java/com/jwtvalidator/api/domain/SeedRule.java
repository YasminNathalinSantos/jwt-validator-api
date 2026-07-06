package com.jwtvalidator.api.domain;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/**
 * A claim Seed deve representar um número primo.
 */
@Component
public class SeedRule implements ClaimValidationRule {

    @Override
    public boolean isValid(JsonNode payload) {
        JsonNode seedNode = payload.get("Seed");
        if (seedNode == null) {
            return false;
        }
        String seedTexto = seedNode.isTextual() ? seedNode.asText() : seedNode.toString();
        try {
            long seed = Long.parseLong(seedTexto.trim());
            return PrimeChecker.isPrime(seed);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String name() {
        return "SeedRule";
    }
}