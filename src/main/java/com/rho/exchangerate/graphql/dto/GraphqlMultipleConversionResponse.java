package com.rho.exchangerate.graphql.dto;

import java.math.BigDecimal;
import java.util.List;

public record GraphqlMultipleConversionResponse(
        String from,
        BigDecimal amount,
        List<ConversionEntry> conversions
) {
}