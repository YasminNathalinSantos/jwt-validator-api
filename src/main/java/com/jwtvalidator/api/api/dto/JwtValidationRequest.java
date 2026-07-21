package com.jwtvalidator.api.api.dto;

import jakarta.validation.constraints.NotBlank;


public class JwtValidationRequest {

    @NotBlank(message = "O campo 'token' é obrigatório e não pode estar em branco")
    private String token;

    public JwtValidationRequest() {
    }

    public JwtValidationRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}