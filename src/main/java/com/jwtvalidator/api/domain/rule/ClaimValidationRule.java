package com.jwtvalidator.api.domain.rule;

import com.fasterxml.jackson.databind.JsonNode;


public interface ClaimValidationRule {

    boolean isValid(JsonNode payload);


    String name();


    String failureReason();
}
