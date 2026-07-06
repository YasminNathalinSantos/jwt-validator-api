package com.jwtvalidator.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtStructuralValidatorTest {

    private final JwtStructuralValidator validator = new JwtStructuralValidator();

    @Test
    void deveConsiderarValidoUmJwtBemFormado() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiQWRtaW4iLCJTZWVkIjoiNzg0MSIsIk5hbWUiOiJUb25pbmhvIEFyYXVqbyJ9.QY05sIjtrcJnP533kQNk8QXcaleJ1Q01jWY_ZzIZuAg";
        assertThat(validator.validate(jwt).isValid()).isTrue();
    }

    @Test
    void deveConsiderarInvalidoUmJwtComSegmentosCorrompidos() {
        String jwt = "eyJhbGciOiJzI1NiJ9.dfsdfsfryJSr2xrIjoiQWRtaW4iLCJTZrkIjoiNzg0MSIsIk5hbrUiOiJUb25pbmhvIEFyYXVqbyJ9.QY05fsdfsIjtrcJnP533kQNk8QXcaleJ1Q01jWY_ZzIZuAg";
        assertThat(validator.validate(jwt).isValid()).isFalse();
    }

    @Test
    void deveConsiderarInvalidoQuandoNaoTemTresPartes() {
        assertThat(validator.validate("apenas.duas").isValid()).isFalse();
    }

    @Test
    void deveConsiderarInvalidoQuandoStringForNulaOuVazia() {
        assertThat(validator.validate(null).isValid()).isFalse();
        assertThat(validator.validate("").isValid()).isFalse();
    }

    @Test
    void deveConsiderarInvalidoQuandoTemSegmentoVazio() {
        assertThat(validator.validate("abc..def").isValid()).isFalse();
    }
}