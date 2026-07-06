package com.jwtvalidator.api.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Verifica se uma string é um JWT bem formado:
 * - possui exatamente 3 segmentos separados por "."
 * - header e payload são Base64URL válidos e decodificam para JSON válido
 * - a assinatura é uma string Base64URL válida (não verificamos a assinatura
 *   criptograficamente, pois nenhuma chave/segredo foi fornecida no enunciado)
 */
@Component
public class JwtStructuralValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public JwtStructureResult validate(String token) {
        if (token == null || token.isBlank()) {
            return JwtStructureResult.invalid();
        }

        String[] parts = token.split("\\.", -1);
        if (parts.length != 3 || parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty()) {
            return JwtStructureResult.invalid();
        }

        try {
            JsonNode header = decodeToJson(parts[0]);
            JsonNode payload = decodeToJson(parts[1]);

            if (header == null || !header.isObject() || payload == null || !payload.isObject()) {
                return JwtStructureResult.invalid();
            }

            Base64.getUrlDecoder().decode(pad(parts[2]));

            return JwtStructureResult.valid(payload);
        } catch (Exception e) {
            return JwtStructureResult.invalid();
        }
    }

    private JsonNode decodeToJson(String segment) throws Exception {
        byte[] decoded = Base64.getUrlDecoder().decode(pad(segment));
        String json = new String(decoded, StandardCharsets.UTF_8);
        return MAPPER.readTree(json);
    }

    private String pad(String base64Url) {
        int remainder = base64Url.length() % 4;
        if (remainder == 0) {
            return base64Url;
        }
        return base64Url + "=".repeat(4 - remainder);
    }
}