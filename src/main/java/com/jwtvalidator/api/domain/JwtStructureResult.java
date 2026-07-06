package com.jwtvalidator.api.domain;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Resultado da checagem estrutural de um JWT.
 * Se válido, carrega o payload (claims) já decodificado como JSON.
 */
public class JwtStructureResult {

    private final boolean valid;
    private final JsonNode payload;

    private JwtStructureResult(boolean valid, JsonNode payload) {
        this.valid = valid;
        this.payload = payload;
    }

    public static JwtStructureResult valid(JsonNode payload) {
        return new JwtStructureResult(true, payload);
    }

    public static JwtStructureResult invalid() {
        return new JwtStructureResult(false, null);
    }

    public boolean isValid() {
        return valid;
    }

    public JsonNode getPayload() {
        return payload;
    }
}