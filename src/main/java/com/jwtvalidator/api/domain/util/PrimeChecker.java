package com.jwtvalidator.api.domain.util;


public final class PrimeChecker {

    private PrimeChecker() {
    }

    public static boolean isPrime(long numero) {
        if (numero < 2) {
            return false;
        }
        if (numero == 2) {
            return true;
        }
        if (numero % 2 == 0) {
            return false;
        }
        for (long i = 3; i * i <= numero; i += 2) {
            if (numero % i == 0) {
                return false;
            }
        }
        return true;
    }
}