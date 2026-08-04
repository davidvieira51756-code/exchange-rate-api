package com.rho.exchangerate.controller;

import com.rho.exchangerate.dto.ConversionResponse;
import com.rho.exchangerate.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.rho.exchangerate.dto.MultipleConversionResponse;
import com.rho.exchangerate.validation.RequestValidator;

import java.math.BigDecimal;
import java.util.List;

@Tag(
        name = "Currency Conversions",
        description = "Convert monetary amounts between currencies"
)
@RestController
@RequestMapping("/api/conversions")
public class ConversionController {

    private final ExchangeRateService exchangeRateService;

    public ConversionController(
            ExchangeRateService exchangeRateService) {

        this.exchangeRateService = exchangeRateService;
    }

    @Operation(
            summary = "Convert an amount to another currency"
    )
    @GetMapping
    public ConversionResponse convertAmount(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {

        RequestValidator.validateCurrencyCode(from);
        RequestValidator.validateCurrencyCode(to);
        RequestValidator.validateAmount(amount);

        return exchangeRateService.convertAmount(
                from,
                to,
                amount
        );
    }

    @Operation(
            summary = "Convert an amount to multiple currencies"
    )
    @GetMapping("/multiple")
    public MultipleConversionResponse convertAmountToMultipleCurrencies(
            @RequestParam String from,
            @RequestParam List<String> to,
            @RequestParam BigDecimal amount) {

        RequestValidator.validateCurrencyCode(from);
        RequestValidator.validateCurrencyCodes(to);
        RequestValidator.validateAmount(amount);

        return exchangeRateService
                .convertAmountToMultipleCurrencies(
                        from,
                        to,
                        amount
                );
    }
}