package com.rho.exchangerate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

//returns a response from a currency to all others
@Data
@AllArgsConstructor
public class AllExchangeRatesResponse {

    private String base;
    private Map<String, BigDecimal> rates;
}