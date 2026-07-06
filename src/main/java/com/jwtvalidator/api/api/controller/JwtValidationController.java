package com.jwtvalidator.api.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtvalidator.api.api.dto.JwtDecodeResponse;
import com.jwtvalidator.api.api.dto.JwtValidationRequest;
import com.jwtvalidator.api.api.dto.JwtValidationResponse;
import com.jwtvalidator.api.application.JwtValidationService;
import com.jwtvalidator.api.domain.JwtStructuralValidator;
import com.jwtvalidator.api.domain.JwtStructureResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Expõe os endpoints relacionados a JWT via HTTP.
 * Esta camada não contém regra de negócio — apenas traduz HTTP <-> Service/Domain.
 */
@RestController
@RequestMapping("/api/v1/jwt")
public class JwtValidationController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JwtValidationService jwtValidationService;
    private final JwtStructuralValidator jwtStructuralValidator;

    public JwtValidationController(JwtValidationService jwtValidationService,
                                    JwtStructuralValidator jwtStructuralValidator) {
        this.jwtValidationService = jwtValidationService;
        this.jwtStructuralValidator = jwtStructuralValidator;
    }

    @PostMapping("/validate")
    public ResponseEntity<JwtValidationResponse> validate(@Valid @RequestBody JwtValidationRequest request) {
        boolean isValid = jwtValidationService.isValid(request.getToken());
        return ResponseEntity.ok(new JwtValidationResponse(isValid));
    }

    /**
     * Endpoint auxiliar de debug/visualização: decodifica o payload de um JWT,
     * sem aplicar as regras de negócio (Name, Role, Seed). Útil para conferir
     * visualmente o conteúdo de um token durante testes manuais.
     */
    @GetMapping("/decode")
    public ResponseEntity<JwtDecodeResponse> decode(@RequestParam String token) {
        JwtStructureResult result = jwtStructuralValidator.validate(token);

        Map<String, Object> payloadAsMap = result.isValid()
                ? MAPPER.convertValue(result.getPayload(), Map.class)
                : null;

        return ResponseEntity.ok(new JwtDecodeResponse(result.isValid(), payloadAsMap));
    }
}
