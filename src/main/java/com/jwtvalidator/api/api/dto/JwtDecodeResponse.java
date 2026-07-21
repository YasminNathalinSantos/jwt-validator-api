package com.jwtvalidator.api.api.dto;

import java.util.Map;


public class JwtDecodeResponse {

    private final boolean structurallyValid;
    private final Map<String, Object> payload;

    public JwtDecodeResponse(boolean structurallyValid, Map<String, Object> payload) {
        this.structurallyValid = structurallyValid;
        this.payload = payload;
    }

    public boolean isStructurallyValid() {
        return structurallyValid;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}
