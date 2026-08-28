package com.rho.exchangerate.dto;

import java.math.BigDecimal;
import java.util.Map;

public record AllExchangeRatesResponse(
        String base,
        Map<String, BigDecimal> rates
) {
}