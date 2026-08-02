package com.rho.exchangerate.controller;

import com.rho.exchangerate.client.ExchangeRateProviderClient;
import com.rho.exchangerate.dto.ProviderRatesResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final ExchangeRateProviderClient exchangeRateProviderClient;

    public HealthController(ExchangeRateProviderClient exchangeRateProviderClient) {
        this.exchangeRateProviderClient = exchangeRateProviderClient;
    }

    @GetMapping("/health")
    public String health() {
        return "Exchange Rate API is running";
    }

    @GetMapping("/test-rates")
    public ProviderRatesResponse testRates() {
        return exchangeRateProviderClient.getLatestRates();
    }
}