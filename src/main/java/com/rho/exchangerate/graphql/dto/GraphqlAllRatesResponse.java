package com.rho.exchangerate.graphql.dto;

import java.util.List;

public record GraphqlAllRatesResponse(
        String base,
        List<RateEntry> rates
) {
}