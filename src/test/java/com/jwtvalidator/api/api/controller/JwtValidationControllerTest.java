package com.jwtvalidator.api.api.controller;

import com.jwtvalidator.api.api.dto.JwtValidationRequest;
import com.jwtvalidator.api.api.dto.JwtValidationResponse;
import com.jwtvalidator.api.application.JwtValidationService;
import com.jwtvalidator.api.domain.ClaimsWhitelistRule;
import com.jwtvalidator.api.domain.JwtStructuralValidator;
import com.jwtvalidator.api.domain.NameRule;
import com.jwtvalidator.api.domain.RoleRule;
import com.jwtvalidator.api.domain.SeedRule;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes do Controller sem subir o contexto completo do Spring:
 * instanciamos o Service real (com as regras reais) e chamamos o
 * Controller diretamente, validando a tradução HTTP <-> Service.
 */
class JwtValidationControllerTest {

    private final JwtValidationService service = new JwtValidationService(
            new JwtStructuralValidator(),
            List.of(new ClaimsWhitelistRule(), new NameRule(), new RoleRule(), new SeedRule())
    );

    private final JwtValidationController controller = new JwtValidationController(service);

    @Test
    void caso1_deveRetornarValidoTrue() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiQWRtaW4iLCJTZWVkIjoiNzg0MSIsIk5hbWUiOiJUb25pbmhvIEFyYXVqbyJ9.QY05sIjtrcJnP533kQNk8QXcaleJ1Q01jWY_ZzIZuAg";

        ResponseEntity<JwtValidationResponse> response = controller.validate(new JwtValidationRequest(jwt));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isValid()).isTrue();
    }

    @Test
    void caso2_deveRetornarValidoFalse_jwtMalformado() {
        String jwt = "eyJhbGciOiJzI1NiJ9.dfsdfsfryJSr2xrIjoiQWRtaW4iLCJTZrkIjoiNzg0MSIsIk5hbrUiOiJUb25pbmhvIEFyYXVqbyJ9.QY05fsdfsIjtrcJnP533kQNk8QXcaleJ1Q01jWY_ZzIZuAg";

        ResponseEntity<JwtValidationResponse> response = controller.validate(new JwtValidationRequest(jwt));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isValid()).isFalse();
    }

    @Test
    void caso3_deveRetornarValidoFalse_nomeComNumero() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiRXh0ZXJuYWwiLCJTZWVkIjoiODgwMzciLCJOYW1lIjoiTTRyaWEgT2xpdmlhIn0.6YD73XWZYQSSMDf6H0i3-kylz1-TY_Yt6h1cV2Ku-Qs";

        ResponseEntity<JwtValidationResponse> response = controller.validate(new JwtValidationRequest(jwt));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isValid()).isFalse();
    }

    @Test
    void caso4_deveRetornarValidoFalse_maisDeTresClaims() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiTWVtYmVyIiwiT3JnIjoiQlIiLCJTZWVkIjoiMTQ2MjciLCJOYW1lIjoiVmFsZGlyIEFyYW5oYSJ9.cmrXV_Flm5mfdpfNUVopY_I2zeJUy4EZ4i3Fea98zvY";

        ResponseEntity<JwtValidationResponse> response = controller.validate(new JwtValidationRequest(jwt));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isValid()).isFalse();
    }
}