package com.rho.exchangerate.dto;

import java.math.BigDecimal;

public record ConversionResponse(
        String from,
        String to,
        BigDecimal amount,
        BigDecimal rate,
        BigDecimal convertedAmount
) {
}