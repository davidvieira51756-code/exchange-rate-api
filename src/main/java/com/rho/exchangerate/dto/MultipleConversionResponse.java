package com.rho.exchangerate.dto;

import java.math.BigDecimal;
import java.util.Map;

public record MultipleConversionResponse(
        String from,
        BigDecimal amount,
        Map<String, BigDecimal> conversions
) {
}