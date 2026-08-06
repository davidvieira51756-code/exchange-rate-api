package com.rho.exchangerate.graphql.dto;

import java.math.BigDecimal;

public record RateEntry(
        String currency,
        BigDecimal rate
) {
}