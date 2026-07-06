package com.jwtvalidator.api.api.controller;

import com.jwtvalidator.api.api.dto.JwtValidationRequest;
import com.jwtvalidator.api.api.dto.JwtValidationResponse;
import com.jwtvalidator.api.application.JwtValidationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expõe o endpoint de validação de JWT via HTTP.
 * Esta camada não contém regra de negócio — apenas traduz HTTP <-> Service.
 */
@RestController
@RequestMapping("/api/v1/jwt")
public class JwtValidationController {

    private final JwtValidationService jwtValidationService;

    public JwtValidationController(JwtValidationService jwtValidationService) {
        this.jwtValidationService = jwtValidationService;
    }

    @PostMapping("/validate")
    public ResponseEntity<JwtValidationResponse> validate(@Valid @RequestBody JwtValidationRequest request) {
        boolean isValid = jwtValidationService.isValid(request.getToken());
        return ResponseEntity.ok(new JwtValidationResponse(isValid));
    }
}