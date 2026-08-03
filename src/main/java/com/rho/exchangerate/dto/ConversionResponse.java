package com.rho.exchangerate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ConversionResponse {

    private String from;
    private String to;
    private BigDecimal amount;
    private BigDecimal rate;
    private BigDecimal convertedAmount;
}