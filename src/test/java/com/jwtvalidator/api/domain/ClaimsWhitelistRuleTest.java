package com.jwtvalidator.api.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClaimsWhitelistRuleTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ClaimsWhitelistRule rule = new ClaimsWhitelistRule();

    @Test
    void deveSerValidoComExatamenteAsTresClaims() throws Exception {
        JsonNode payload = mapper.readTree("{\"Role\":\"Admin\",\"Seed\":\"7841\",\"Name\":\"Toninho Araujo\"}");
        assertThat(rule.isValid(payload)).isTrue();
    }

    @Test
    void deveSerInvalidoComClaimExtra() throws Exception {
        JsonNode payload = mapper.readTree("{\"Role\":\"Member\",\"Org\":\"BR\",\"Seed\":\"14627\",\"Name\":\"Valdir Aranha\"}");
        assertThat(rule.isValid(payload)).isFalse();
    }

    @Test
    void deveSerInvalidoComClaimFaltando() throws Exception {
        JsonNode payload = mapper.readTree("{\"Role\":\"Admin\",\"Seed\":\"7841\"}");
        assertThat(rule.isValid(payload)).isFalse();
    }

    @Test
    void deveSerInvalidoComNomeDeClaimErrado() throws Exception {
        JsonNode payload = mapper.readTree("{\"role\":\"Admin\",\"Seed\":\"7841\",\"Name\":\"Toninho\"}");
        assertThat(rule.isValid(payload)).isFalse();
    }
}