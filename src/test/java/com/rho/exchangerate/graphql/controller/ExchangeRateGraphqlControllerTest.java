package com.rho.exchangerate.graphql.controller;

import com.rho.exchangerate.dto.AllExchangeRatesResponse;
import com.rho.exchangerate.dto.ConversionResponse;
import com.rho.exchangerate.dto.ExchangeRateResponse;
import com.rho.exchangerate.dto.MultipleConversionResponse;
import com.rho.exchangerate.graphql.dto.GraphqlAllRatesResponse;
import com.rho.exchangerate.graphql.dto.GraphqlMultipleConversionResponse;
import com.rho.exchangerate.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ExchangeRateGraphqlControllerTest {

    private ExchangeRateService exchangeRateService;
    private ExchangeRateGraphqlController controller;

    @BeforeEach
    void setUp() {
        exchangeRateService = Mockito.mock(ExchangeRateService.class);
        controller = new ExchangeRateGraphqlController(exchangeRateService);
    }

    @Test
    void shouldReturnExchangeRate() {
        ExchangeRateResponse serviceResponse =
                new ExchangeRateResponse(
                        "EUR",
                        "GBP",
                        new BigDecimal("0.7500000000")
                );

        when(exchangeRateService.getExchangeRate("EUR", "GBP"))
                .thenReturn(serviceResponse);

        ExchangeRateResponse response =
                controller.exchangeRate("EUR", "GBP");

        assertEquals("EUR", response.getFrom());
        assertEquals("GBP", response.getTo());
        assertEquals(
                new BigDecimal("0.7500000000"),
                response.getRate()
        );
    }

    @Test
    void shouldReturnAllRates() {
        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        rates.put("USD", new BigDecimal("1.2500000000"));
        rates.put("GBP", new BigDecimal("0.7500000000"));

        AllExchangeRatesResponse serviceResponse =
                new AllExchangeRatesResponse("EUR", rates);

        when(exchangeRateService.getAllRates("EUR"))
                .thenReturn(serviceResponse);

        GraphqlAllRatesResponse response =
                controller.allRates("EUR");

        assertEquals("EUR", response.base());

        assertEquals("USD", response.rates().get(0).currency());
        assertEquals(
                new BigDecimal("1.2500000000"),
                response.rates().get(0).rate()
        );

        assertEquals("GBP", response.rates().get(1).currency());
        assertEquals(
                new BigDecimal("0.7500000000"),
                response.rates().get(1).rate()
        );
    }

    @Test
    void shouldConvertAmount() {
        ConversionResponse serviceResponse =
                new ConversionResponse(
                        "EUR",
                        "GBP",
                        new BigDecimal("100"),
                        new BigDecimal("0.7500000000"),
                        new BigDecimal("75.0000000000")
                );

        when(exchangeRateService.convertAmount(
                "EUR",
                "GBP",
                new BigDecimal("100")
        )).thenReturn(serviceResponse);

        ConversionResponse response =
                controller.convert("EUR", "GBP", "100");

        assertEquals("EUR", response.getFrom());
        assertEquals("GBP", response.getTo());

        assertEquals(
                new BigDecimal("100"),
                response.getAmount()
        );

        assertEquals(
                new BigDecimal("0.7500000000"),
                response.getRate()
        );

        assertEquals(
                new BigDecimal("75.0000000000"),
                response.getConvertedAmount()
        );
    }

    @Test
    void shouldConvertAmountToMultipleCurrencies() {
        Map<String, BigDecimal> conversions = new LinkedHashMap<>();
        conversions.put("GBP", new BigDecimal("75.0000000000"));
        conversions.put("USD", new BigDecimal("125.0000000000"));

        MultipleConversionResponse serviceResponse =
                new MultipleConversionResponse(
                        "EUR",
                        new BigDecimal("100"),
                        conversions
                );

        when(exchangeRateService.convertAmountToMultipleCurrencies(
                "EUR",
                List.of("GBP", "USD"),
                new BigDecimal("100")
        )).thenReturn(serviceResponse);

        GraphqlMultipleConversionResponse response =
                controller.convertMultiple(
                        "EUR",
                        List.of("GBP", "USD"),
                        "100"
                );

        assertEquals("EUR", response.from());

        assertEquals(
                new BigDecimal("100"),
                response.amount()
        );

        assertEquals("GBP", response.conversions().get(0).currency());
        assertEquals(
                new BigDecimal("75.0000000000"),
                response.conversions().get(0).convertedAmount()
        );

        assertEquals("USD", response.conversions().get(1).currency());
        assertEquals(
                new BigDecimal("125.0000000000"),
                response.conversions().get(1).convertedAmount()
        );
    }
}