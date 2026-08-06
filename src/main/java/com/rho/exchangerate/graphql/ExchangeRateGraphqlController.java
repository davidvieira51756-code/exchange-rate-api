package com.rho.exchangerate.graphql;

import com.rho.exchangerate.dto.ConversionResponse;
import com.rho.exchangerate.dto.ExchangeRateResponse;
import com.rho.exchangerate.service.ExchangeRateService;
import com.rho.exchangerate.validation.RequestValidator;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class ExchangeRateGraphqlController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateGraphqlController(
            ExchangeRateService exchangeRateService
    ) {
        this.exchangeRateService = exchangeRateService;
    }

    @QueryMapping
    public ExchangeRateResponse exchangeRate(
            @Argument String from,
            @Argument String to
    ) {
        RequestValidator.validateCurrencyCode(from);
        RequestValidator.validateCurrencyCode(to);

        return exchangeRateService.getExchangeRate(from, to);
    }

    @QueryMapping
    public ConversionResponse convert(
            @Argument String from,
            @Argument String to,
            @Argument String amount
    ) {
        RequestValidator.validateCurrencyCode(from);
        RequestValidator.validateCurrencyCode(to);

        BigDecimal parsedAmount;

        try {
            parsedAmount = new BigDecimal(amount);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Amount must be a valid number"
            );
        }

        RequestValidator.validateAmount(parsedAmount);

        return exchangeRateService.convertAmount(
                from,
                to,
                parsedAmount
        );
    }
}