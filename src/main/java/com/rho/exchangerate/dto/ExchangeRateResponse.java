package com.rho.exchangerate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

//JSON returned to the user by the API
@Data
@AllArgsConstructor
public class ExchangeRateResponse {

    private String from;
    private String to;
    private BigDecimal rate;
}