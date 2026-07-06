package com.jwtvalidator.api.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class RoleRuleTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RoleRule rule = new RoleRule();

    @ParameterizedTest
    @ValueSource(strings = {"Admin", "Member", "External"})
    void deveSerValidoParaCadaRolePermitida(String role) throws Exception {
        JsonNode payload = mapper.readTree("{\"Role\":\"" + role + "\"}");
        assertThat(rule.isValid(payload)).isTrue();
    }

    @Test
    void deveSerInvalidoParaRoleDesconhecida() throws Exception {
        JsonNode payload = mapper.readTree("{\"Role\":\"SuperAdmin\"}");
        assertThat(rule.isValid(payload)).isFalse();
    }

    @Test
    void deveSerInvalidoQuandoClaimNaoExiste() throws Exception {
        assertThat(rule.isValid(mapper.readTree("{}"))).isFalse();
    }
}