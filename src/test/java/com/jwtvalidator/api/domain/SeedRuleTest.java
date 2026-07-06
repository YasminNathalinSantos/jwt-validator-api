package com.jwtvalidator.api.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeedRuleTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final SeedRule rule = new SeedRule();

    @Test
    void deveSerValidoParaSeedPrimo() throws Exception {
        JsonNode payload = mapper.readTree("{\"Seed\":\"7841\"}");
        assertThat(rule.isValid(payload)).isTrue();
    }

    @Test
    void deveSerInvalidoParaSeedNaoPrimo() throws Exception {
        // 12345 é divisível por 5, portanto não é primo
        JsonNode payload = mapper.readTree("{\"Seed\":\"12345\"}");
        assertThat(rule.isValid(payload)).isFalse();
    }

    @Test
    void deveSerInvalidoParaSeedNaoNumerico() throws Exception {
        JsonNode payload = mapper.readTree("{\"Seed\":\"abc\"}");
        assertThat(rule.isValid(payload)).isFalse();
    }

    @Test
    void deveSerInvalidoQuandoClaimNaoExiste() throws Exception {
        JsonNode payload = mapper.readTree("{}");
        assertThat(rule.isValid(payload)).isFalse();
    }
}