package com.rho.exchangerate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class MultipleConversionResponse {

    private String from;
    private BigDecimal amount;
    private Map<String, BigDecimal> conversions;
}