package com.jwtvalidator.api.api.dto;

/**
 * Corpo da resposta com o resultado da validação.
 */
public class JwtValidationResponse {

    private final boolean valid;

    public JwtValidationResponse(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }
}