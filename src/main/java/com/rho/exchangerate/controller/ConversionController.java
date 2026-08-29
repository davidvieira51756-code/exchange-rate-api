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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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
            @RequestParam
            @Pattern(
                    regexp = "^[A-Za-z]{3}$",
                    message = "Currency must contain exactly 3 letters"
            )
            String from,

            @RequestParam
            @Pattern(
                    regexp = "^[A-Za-z]{3}$",
                    message = "Currency must contain exactly 3 letters"
            )
            String to,

            @RequestParam
            @Positive(message = "Amount must be greater than zero")
            BigDecimal amount) {

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
            @RequestParam
            @Pattern(
                    regexp = "^[A-Za-z]{3}$",
                    message = "Currency must contain exactly 3 letters"
            )
            String from,

            @RequestParam
            @Size(
                    min = 1,
                    message = "At least one target currency must be supplied"
            )
            List<
                    @Pattern(
                            regexp = "^[A-Za-z]{3}$",
                            message = "Currency must contain exactly 3 letters"
                    )
                            String
                    > to,

            @RequestParam
            @Positive(message = "Amount must be greater than zero")
            BigDecimal amount) {

        return exchangeRateService
                .convertAmountToMultipleCurrencies(
                        from,
                        to,
                        amount
                );
    }
}