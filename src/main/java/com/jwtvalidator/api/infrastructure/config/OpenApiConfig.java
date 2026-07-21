package com.jwtvalidator.api.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jwtValidatorOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("JWT Validator API")
                        .description("API que valida JWTs conforme regras de negocio customizadas (claims Name, Role e Seed).")
                        .version("v1.0.0"));
    }
}