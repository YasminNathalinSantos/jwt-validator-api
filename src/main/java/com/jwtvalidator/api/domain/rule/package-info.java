/**
 * Implementacoes de ClaimValidationRule: cada classe valida uma claim ou
 * conjunto de claims especifico (Name, Role, Seed, whitelist de claims).
 * Novas regras podem ser adicionadas aqui sem alterar o orquestrador
 * (JwtValidationService), respeitando o principio Open/Closed.
 */
package com.jwtvalidator.api.domain.rule;
