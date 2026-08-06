package com.rho.exchangerate.graphql.dto;

import java.math.BigDecimal;

public record ConversionEntry(
        String currency,
        BigDecimal convertedAmount
) {
}