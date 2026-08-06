package com.rho.exchangerate.graphql.controller;

import com.rho.exchangerate.dto.AllExchangeRatesResponse;
import com.rho.exchangerate.dto.ConversionResponse;
import com.rho.exchangerate.dto.ExchangeRateResponse;
import com.rho.exchangerate.dto.MultipleConversionResponse;
import com.rho.exchangerate.graphql.dto.ConversionEntry;
import com.rho.exchangerate.graphql.dto.GraphqlAllRatesResponse;
import com.rho.exchangerate.graphql.dto.GraphqlMultipleConversionResponse;
import com.rho.exchangerate.graphql.dto.RateEntry;
import com.rho.exchangerate.service.ExchangeRateService;
import com.rho.exchangerate.validation.RequestValidator;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public GraphqlAllRatesResponse allRates(
            @Argument String from
    ) {
        RequestValidator.validateCurrencyCode(from);

        AllExchangeRatesResponse response =
                exchangeRateService.getAllRates(from);

        List<RateEntry> rates = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry
                : response.getRates().entrySet()) {

            rates.add(new RateEntry(
                    entry.getKey(),
                    entry.getValue()
            ));
        }

        return new GraphqlAllRatesResponse(
                response.getBase(),
                rates
        );
    }

    @QueryMapping
    public ConversionResponse convert(
            @Argument String from,
            @Argument String to,
            @Argument String amount
    ) {
        RequestValidator.validateCurrencyCode(from);
        RequestValidator.validateCurrencyCode(to);

        BigDecimal parsedAmount = parseAmount(amount);

        return exchangeRateService.convertAmount(
                from,
                to,
                parsedAmount
        );
    }

    @QueryMapping
    public GraphqlMultipleConversionResponse convertMultiple(
            @Argument String from,
            @Argument List<String> to,
            @Argument String amount
    ) {
        RequestValidator.validateCurrencyCode(from);
        RequestValidator.validateCurrencyCodes(to);

        BigDecimal parsedAmount = parseAmount(amount);

        MultipleConversionResponse response =
                exchangeRateService.convertAmountToMultipleCurrencies(
                        from,
                        to,
                        parsedAmount
                );

        List<ConversionEntry> conversions = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry
                : response.getConversions().entrySet()) {

            conversions.add(new ConversionEntry(
                    entry.getKey(),
                    entry.getValue()
            ));
        }

        return new GraphqlMultipleConversionResponse(
                response.getFrom(),
                response.getAmount(),
                conversions
        );
    }

    private BigDecimal parseAmount(String amount) {
        BigDecimal parsedAmount;

        try {
            parsedAmount = new BigDecimal(amount);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Amount must be a valid number"
            );
        }

        RequestValidator.validateAmount(parsedAmount);

        return parsedAmount;
    }
}