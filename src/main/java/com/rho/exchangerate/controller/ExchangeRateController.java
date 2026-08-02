package com.rho.exchangerate.controller;

import com.rho.exchangerate.dto.ExchangeRateResponse;
import com.rho.exchangerate.service.ExchangeRateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(
            ExchangeRateService exchangeRateService) {

        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/{from}/{to}")
    public ExchangeRateResponse getExchangeRate(
            @PathVariable String from,
            @PathVariable String to) {

        return exchangeRateService.getExchangeRate(from, to);
    }
}