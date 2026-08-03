package com.rho.exchangerate.controller;

import com.rho.exchangerate.dto.ConversionResponse;
import com.rho.exchangerate.service.ExchangeRateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/conversions")
public class ConversionController {

    private final ExchangeRateService exchangeRateService;

    public ConversionController(
            ExchangeRateService exchangeRateService) {

        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public ConversionResponse convertAmount(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {

        return exchangeRateService.convertAmount(
                from,
                to,
                amount
        );
    }
}