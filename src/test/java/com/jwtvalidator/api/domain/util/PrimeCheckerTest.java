package com.jwtvalidator.api.domain.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeCheckerTest {

    @ParameterizedTest
    @ValueSource(longs = {2, 3, 5, 7, 11, 13, 7841})
    void devemSerConsideradosPrimos(long numero) {
        assertThat(PrimeChecker.isPrime(numero)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 4, 6, 8, 9, 100, -7})
    void naoDevemSerConsideradosPrimos(long numero) {
        assertThat(PrimeChecker.isPrime(numero)).isFalse();
    }
}