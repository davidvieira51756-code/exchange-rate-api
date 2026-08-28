package com.rho.exchangerate.graphql.controller;

import com.rho.exchangerate.dto.AllExchangeRatesResponse;
import com.rho.exchangerate.dto.ConversionResponse;
import com.rho.exchangerate.dto.ExchangeRateResponse;
import com.rho.exchangerate.dto.MultipleConversionResponse;
import com.rho.exchangerate.graphql.dto.GraphqlAllRatesResponse;
import com.rho.exchangerate.graphql.dto.GraphqlMultipleConversionResponse;
import com.rho.exchangerate.graphql.mapper.GraphqlInputParser;
import com.rho.exchangerate.graphql.mapper.GraphqlResponseMapper;
import com.rho.exchangerate.service.ExchangeRateService;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

@Controller
@Validated
public class ExchangeRateGraphqlController {

    private final ExchangeRateService exchangeRateService;
    private final GraphqlInputParser inputParser;
    private final GraphqlResponseMapper responseMapper;

    public ExchangeRateGraphqlController(
            ExchangeRateService exchangeRateService,
            GraphqlInputParser inputParser,
            GraphqlResponseMapper responseMapper
    ) {
        this.exchangeRateService = exchangeRateService;
        this.inputParser = inputParser;
        this.responseMapper = responseMapper;
    }

    @QueryMapping
    public ExchangeRateResponse exchangeRate(
            @Argument
            @Pattern(
                    regexp = "^[A-Za-z]{3}$",
                    message = "Currency must contain exactly 3 letters"
            )
            String from,

            @Argument
            @Pattern(
                    regexp = "^[A-Za-z]{3}$",
                    message = "Currency must contain exactly 3 letters"
            )
            String to
    ) {
        return exchangeRateService.getExchangeRate(from, to);
    }

    @QueryMapping
    public GraphqlAllRatesResponse allRates(
            @Argument
            @Pattern(
                    regexp = "^[A-Za-z]{3}$",
                    message = "Currency must contain exactly 3 letters"
            )
            String from
    ) {
        AllExchangeRatesResponse response =
                exchangeRateService.getAllRates(from);

        return responseMapper.toGraphqlAllRatesResponse(response);
    }

    @QueryMapping
    public ConversionResponse convert(
            @Argument
            @Pattern(
                    regexp = "^[A-Za-z]{3}$",
                    message = "Currency must contain exactly 3 letters"
            )
            String from,

            @Argument
            @Pattern(
                    regexp = "^[A-Za-z]{3}$",
                    message = "Currency must contain exactly 3 letters"
            )
            String to,

            @Argument String amount
    ) {
        BigDecimal parsedAmount = inputParser.parseAmount(amount);

        return exchangeRateService.convertAmount(
                from,
                to,
                parsedAmount
        );
    }

    @QueryMapping
    public GraphqlMultipleConversionResponse convertMultiple(
            @Argument
            @Pattern(
                    regexp = "^[A-Za-z]{3}$",
                    message = "Currency must contain exactly 3 letters"
            )
            String from,

            @Argument
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

            @Argument String amount
    ) {
        BigDecimal parsedAmount = inputParser.parseAmount(amount);

        MultipleConversionResponse response =
                exchangeRateService.convertAmountToMultipleCurrencies(
                        from,
                        to,
                        parsedAmount
                );

        return responseMapper
                .toGraphqlMultipleConversionResponse(response);
    }
}