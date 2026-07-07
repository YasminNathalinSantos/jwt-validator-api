package com.jwtvalidator.api.domain.rule;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NameRuleTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final NameRule rule = new NameRule();

    @Test
    void deveSerValidoParaNomeSemNumeros() throws Exception {
        JsonNode payload = mapper.readTree("{\"Name\":\"Toninho Araujo\"}");
        assertThat(rule.isValid(payload)).isTrue();
    }

    @Test
    void deveSerInvalidoParaNomeComNumero() throws Exception {
        JsonNode payload = mapper.readTree("{\"Name\":\"M4ria Olivia\"}");
        assertThat(rule.isValid(payload)).isFalse();
    }

    @Test
    void deveSerInvalidoParaNomeMaiorQue256Caracteres() throws Exception {
        String nomeGigante = "A".repeat(257);
        JsonNode payload = mapper.readTree("{\"Name\":\"" + nomeGigante + "\"}");
        assertThat(rule.isValid(payload)).isFalse();
    }

    @Test
    void deveSerValidoComExatamente256Caracteres() throws Exception {
        String nomeLimite = "A".repeat(256);
        JsonNode payload = mapper.readTree("{\"Name\":\"" + nomeLimite + "\"}");
        assertThat(rule.isValid(payload)).isTrue();
    }

    @Test
    void deveSerInvalidoQuandoClaimNaoExiste() throws Exception {
        JsonNode payload = mapper.readTree("{}");
        assertThat(rule.isValid(payload)).isFalse();
    }
}