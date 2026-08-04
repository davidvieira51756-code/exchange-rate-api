package com.rho.exchangerate.service;

import com.rho.exchangerate.client.ExchangeRateProviderClient;
import com.rho.exchangerate.dto.ExchangeRateResponse;
import com.rho.exchangerate.dto.ProviderRatesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ExchangeRateServiceTest {

    private ExchangeRateProviderClient providerClient;
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        providerClient = Mockito.mock(ExchangeRateProviderClient.class);
        exchangeRateService = new ExchangeRateService(providerClient);
    }

    @Test
    void shouldCalculateExchangeRateBetweenTwoCurrencies() {

        Map<String, BigDecimal> quotes = new HashMap<>();
        quotes.put("USDEUR", new BigDecimal("0.80"));
        quotes.put("USDGBP", new BigDecimal("0.60"));

        ProviderRatesResponse providerResponse =
                new ProviderRatesResponse();

        providerResponse.setQuotes(quotes);

        when(providerClient.getLatestRates())
                .thenReturn(providerResponse);

        ExchangeRateResponse response =
                exchangeRateService.getExchangeRate("EUR", "GBP");

        assertEquals("EUR", response.getFrom());
        assertEquals("GBP", response.getTo());
        assertEquals(
                new BigDecimal("0.7500000000"),
                response.getRate()
        );
    }
}