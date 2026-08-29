package com.rho.exchangerate.controller;

import com.rho.exchangerate.dto.AllExchangeRatesResponse;
import com.rho.exchangerate.dto.ExchangeRateResponse;
import com.rho.exchangerate.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.Pattern;

@Tag(
        name = "Exchange Rates",
        description = "Retrieve exchange rates between currencies"
)
@RestController
@RequestMapping("/api/rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(
            ExchangeRateService exchangeRateService) {

        this.exchangeRateService = exchangeRateService;
    }

    @Operation(
            summary = "Get an exchange rate between two currencies"
    )
    @GetMapping("/{from}/{to}")
    public ExchangeRateResponse getExchangeRate(
            @PathVariable
            @Pattern(
                    regexp = "^[A-Za-z]{3}$",
                    message = "Currency must contain exactly 3 letters"
            )
            String from,

            @PathVariable
            @Pattern(
                    regexp = "^[A-Za-z]{3}$",
                    message = "Currency must contain exactly 3 letters"
            )
            String to) {

        return exchangeRateService.getExchangeRate(from, to);
    }

    @Operation(
            summary = "Get all exchange rates from a base currency"
    )
    @GetMapping("/{from}")
    public AllExchangeRatesResponse getAllRates(
            @PathVariable
            @Pattern(
                    regexp = "^[A-Za-z]{3}$",
                    message = "Currency must contain exactly 3 letters"
            )
            String from) {

        return exchangeRateService.getAllRates(from);
    }
}