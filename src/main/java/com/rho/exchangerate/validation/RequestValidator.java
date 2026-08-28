package com.rho.exchangerate.validation;

import com.rho.exchangerate.exception.InvalidAmountException;
import com.rho.exchangerate.exception.InvalidAmountException;
import com.rho.exchangerate.exception.InvalidCurrencyException;

import java.math.BigDecimal;
import java.util.List;

public final class RequestValidator {

    private static final String CURRENCY_CODE_PATTERN = "^[A-Za-z]{3}$";

    private RequestValidator() {
    }

    public static void validateCurrencyCode(String currency) {
        if (currency == null || !currency.matches(CURRENCY_CODE_PATTERN)) {
            throw new InvalidCurrencyException(
                    "Currency must contain exactly 3 letters"
            );
        }
    }

    public static void validateCurrencyCodes(List<String> currencies) {
        if (currencies == null || currencies.isEmpty()) {
            throw new InvalidCurrencyException(
                    "At least one target currency must be supplied"
            );
        }

        for (String currency : currencies) {
            validateCurrencyCode(currency);
        }
    }

    public static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Amount must be greater than zero"
            );
        }
    }
}