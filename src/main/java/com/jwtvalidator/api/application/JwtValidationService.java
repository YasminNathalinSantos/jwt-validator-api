package com.jwtvalidator.api.application;

import com.jwtvalidator.api.domain.rule.ClaimValidationRule;
import com.jwtvalidator.api.domain.JwtStructuralValidator;
import com.jwtvalidator.api.domain.JwtStructureResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class JwtValidationService {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationService.class);
    private static final String METRIC_NAME = "jwt.validation.count";

    private final JwtStructuralValidator structuralValidator;
    private final List<ClaimValidationRule> regras;
    private final MeterRegistry meterRegistry;

    public JwtValidationService(JwtStructuralValidator structuralValidator,
                                 List<ClaimValidationRule> regras,
                                 MeterRegistry meterRegistry) {
        this.structuralValidator = structuralValidator;
        this.regras = regras;
        this.meterRegistry = meterRegistry;
    }

    public boolean isValid(String token) {
        JwtStructureResult estrutura = structuralValidator.validate(token);

        if (!estrutura.isValid()) {
            String motivo = "O token nao e um JWT bem formado (estrutura ou codificacao invalida)";
            log.info("Validacao de JWT falhou: {}", motivo);
            registrarMetrica("invalid", "structure", motivo);
            return false;
        }

        for (ClaimValidationRule regra : regras) {
            if (!regra.isValid(estrutura.getPayload())) {
                log.info("Validacao de JWT falhou na regra '{}': {}", regra.name(), regra.failureReason());
                registrarMetrica("invalid", regra.name(), regra.failureReason());
                return false;
            }
        }

        log.info("JWT validado com sucesso");
        registrarMetrica("valid", "none", "Todas as regras foram atendidas");
        return true;
    }

    private void registrarMetrica(String resultado, String motivoCurto, String motivoDescritivo) {
        Counter.builder(METRIC_NAME)
                .description("Contagem de validacoes de JWT, por resultado e motivo de falha")
                .tag("result", resultado)
                .tag("reason", motivoCurto)
                .tag("reason_description", motivoDescritivo)
                .register(meterRegistry)
                .increment();
    }
}
