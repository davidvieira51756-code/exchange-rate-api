package com.rho.exchangerate.validation;

import java.math.BigDecimal;
import java.util.List;

public final class RequestValidator {

    private static final String CURRENCY_CODE_PATTERN = "^[A-Za-z]{3}$";

    private RequestValidator() {
    }

    public static void validateCurrencyCode(String currency) {
        if (currency == null || !currency.matches(CURRENCY_CODE_PATTERN)) {
            throw new IllegalArgumentException(
                    "Currency must contain exactly 3 letters"
            );
        }
    }

    public static void validateCurrencyCodes(List<String> currencies) {
        if (currencies == null || currencies.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one target currency must be supplied"
            );
        }

        for (String currency : currencies) {
            validateCurrencyCode(currency);
        }
    }

    public static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }
    }
}