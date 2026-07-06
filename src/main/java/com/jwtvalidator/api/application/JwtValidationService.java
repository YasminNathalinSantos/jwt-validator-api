package com.jwtvalidator.api.application;

import com.jwtvalidator.api.domain.ClaimValidationRule;
import com.jwtvalidator.api.domain.JwtStructuralValidator;
import com.jwtvalidator.api.domain.JwtStructureResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orquestra a validação completa de um JWT:
 * 1) valida a estrutura do token
 * 2) aplica todas as regras de claims registradas (injetadas pelo Spring)
 *
 * Novas regras podem ser adicionadas apenas criando uma nova classe que
 * implementa ClaimValidationRule — nenhuma alteração é necessária aqui (OCP).
 */
@Service
public class JwtValidationService {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationService.class);

    private final JwtStructuralValidator structuralValidator;
    private final List<ClaimValidationRule> regras;

    public JwtValidationService(JwtStructuralValidator structuralValidator,
                                 List<ClaimValidationRule> regras) {
        this.structuralValidator = structuralValidator;
        this.regras = regras;
    }

    public boolean isValid(String token) {
        JwtStructureResult estrutura = structuralValidator.validate(token);

        if (!estrutura.isValid()) {
            log.info("Validação de JWT falhou: estrutura inválida");
            return false;
        }

        for (ClaimValidationRule regra : regras) {
            if (!regra.isValid(estrutura.getPayload())) {
                log.info("Validação de JWT falhou na regra: {}", regra.name());
                return false;
            }
        }

        log.info("JWT validado com sucesso");
        return true;
    }
}