package com.rho.exchangerate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProviderRatesResponse {

    private boolean success;
    private String source;
    private Map<String, BigDecimal> quotes;
}