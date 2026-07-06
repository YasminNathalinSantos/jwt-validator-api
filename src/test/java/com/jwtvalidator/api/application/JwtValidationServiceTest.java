package com.jwtvalidator.api.application;

import com.jwtvalidator.api.domain.ClaimsWhitelistRule;
import com.jwtvalidator.api.domain.JwtStructuralValidator;
import com.jwtvalidator.api.domain.NameRule;
import com.jwtvalidator.api.domain.RoleRule;
import com.jwtvalidator.api.domain.SeedRule;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtValidationServiceTest {

    private final JwtValidationService service = new JwtValidationService(
            new JwtStructuralValidator(),
            List.of(new ClaimsWhitelistRule(), new NameRule(), new RoleRule(), new SeedRule()),
            new SimpleMeterRegistry()
    );

    @Test
    void caso1_deveSerValido() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiQWRtaW4iLCJTZWVkIjoiNzg0MSIsIk5hbWUiOiJUb25pbmhvIEFyYXVqbyJ9.QY05sIjtrcJnP533kQNk8QXcaleJ1Q01jWY_ZzIZuAg";
        assertThat(service.isValid(jwt)).isTrue();
    }

    @Test
    void caso2_deveSerInvalido_jwtMalformado() {
        String jwt = "eyJhbGciOiJzI1NiJ9.dfsdfsfryJSr2xrIjoiQWRtaW4iLCJTZrkIjoiNzg0MSIsIk5hbrUiOiJUb25pbmhvIEFyYXVqbyJ9.QY05fsdfsIjtrcJnP533kQNk8QXcaleJ1Q01jWY_ZzIZuAg";
        assertThat(service.isValid(jwt)).isFalse();
    }

    @Test
    void caso3_deveSerInvalido_nomeComNumero() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiRXh0ZXJuYWwiLCJTZWVkIjoiODgwMzciLCJOYW1lIjoiTTRyaWEgT2xpdmlhIn0.6YD73XWZYQSSMDf6H0i3-kylz1-TY_Yt6h1cV2Ku-Qs";
        assertThat(service.isValid(jwt)).isFalse();
    }

    @Test
    void caso4_deveSerInvalido_maisDeTresClaims() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiTWVtYmVyIiwiT3JnIjoiQlIiLCJTZWVkIjoiMTQ2MjciLCJOYW1lIjoiVmFsZGlyIEFyYW5oYSJ9.cmrXV_Flm5mfdpfNUVopY_I2zeJUy4EZ4i3Fea98zvY";
        assertThat(service.isValid(jwt)).isFalse();
    }
}
