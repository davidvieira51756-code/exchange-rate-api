package com.rho.exchangerate.service;

import com.rho.exchangerate.client.ExchangeRateProviderClient;
import com.rho.exchangerate.dto.AllExchangeRatesResponse;
import com.rho.exchangerate.dto.ConversionResponse;
import com.rho.exchangerate.dto.ExchangeRateResponse;
import com.rho.exchangerate.dto.MultipleConversionResponse;
import com.rho.exchangerate.dto.ProviderRatesResponse;
import com.rho.exchangerate.exception.UnsupportedCurrencyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ExchangeRateServiceTest {

    private ExchangeRateProviderClient providerClient;
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        providerClient =
                Mockito.mock(
                        ExchangeRateProviderClient.class
                );

        exchangeRateService =
                new ExchangeRateService(
                        providerClient
                );
    }

    @Test
    void shouldCalculateExchangeRateBetweenTwoCurrencies() {
        Map<String, BigDecimal> quotes =
                new HashMap<>();

        quotes.put(
                "USDEUR",
                new BigDecimal("0.80")
        );

        quotes.put(
                "USDGBP",
                new BigDecimal("0.60")
        );

        ProviderRatesResponse providerResponse =
                new ProviderRatesResponse();

        providerResponse.setQuotes(quotes);

        when(providerClient.getLatestRates())
                .thenReturn(providerResponse);

        ExchangeRateResponse response =
                exchangeRateService
                        .getExchangeRate(
                                "EUR",
                                "GBP"
                        );

        assertEquals(
                "EUR",
                response.from()
        );

        assertEquals(
                "GBP",
                response.to()
        );

        assertEquals(
                new BigDecimal("0.7500000000"),
                response.rate()
        );
    }

    @Test
    void shouldCalculateExchangeRateWhenSourceCurrencyIsUsd() {
        Map<String, BigDecimal> quotes =
                new HashMap<>();

        quotes.put(
                "USDEUR",
                new BigDecimal("0.80")
        );

        ProviderRatesResponse providerResponse =
                new ProviderRatesResponse();

        providerResponse.setQuotes(quotes);

        when(providerClient.getLatestRates())
                .thenReturn(providerResponse);

        ExchangeRateResponse response =
                exchangeRateService
                        .getExchangeRate(
                                "USD",
                                "EUR"
                        );

        assertEquals(
                "USD",
                response.from()
        );

        assertEquals(
                "EUR",
                response.to()
        );

        assertEquals(
                new BigDecimal("0.8000000000"),
                response.rate()
        );
    }

    @Test
    void shouldCalculateAllExchangeRatesFromGivenCurrency() {
        Map<String, BigDecimal> quotes =
                new HashMap<>();

        quotes.put(
                "USDEUR",
                new BigDecimal("0.80")
        );

        quotes.put(
                "USDGBP",
                new BigDecimal("0.60")
        );

        quotes.put(
                "USDJPY",
                new BigDecimal("120.00")
        );

        ProviderRatesResponse providerResponse =
                new ProviderRatesResponse();

        providerResponse.setQuotes(quotes);

        when(providerClient.getLatestRates())
                .thenReturn(providerResponse);

        AllExchangeRatesResponse response =
                exchangeRateService
                        .getAllRates("EUR");

        assertEquals(
                "EUR",
                response.base()
        );

        assertEquals(
                new BigDecimal("1"),
                response.rates().get("EUR")
        );

        assertEquals(
                new BigDecimal("1.2500000000"),
                response.rates().get("USD")
        );

        assertEquals(
                new BigDecimal("0.7500000000"),
                response.rates().get("GBP")
        );

        assertEquals(
                new BigDecimal("150.0000000000"),
                response.rates().get("JPY")
        );
    }

    @Test
    void shouldConvertAmountBetweenTwoCurrencies() {
        Map<String, BigDecimal> quotes =
                new HashMap<>();

        quotes.put(
                "USDEUR",
                new BigDecimal("0.80")
        );

        quotes.put(
                "USDGBP",
                new BigDecimal("0.60")
        );

        ProviderRatesResponse providerResponse =
                new ProviderRatesResponse();

        providerResponse.setQuotes(quotes);

        when(providerClient.getLatestRates())
                .thenReturn(providerResponse);

        ConversionResponse response =
                exchangeRateService.convertAmount(
                        "EUR",
                        "GBP",
                        new BigDecimal("100")
                );

        assertEquals(
                "EUR",
                response.from()
        );

        assertEquals(
                "GBP",
                response.to()
        );

        assertEquals(
                new BigDecimal("100"),
                response.amount()
        );

        assertEquals(
                new BigDecimal("75.0000000000"),
                response.convertedAmount()
        );
    }

    @Test
    void shouldConvertAmountToMultipleCurrencies() {
        Map<String, BigDecimal> quotes =
                new HashMap<>();

        quotes.put(
                "USDEUR",
                new BigDecimal("0.80")
        );

        quotes.put(
                "USDGBP",
                new BigDecimal("0.60")
        );

        quotes.put(
                "USDJPY",
                new BigDecimal("120.00")
        );

        ProviderRatesResponse providerResponse =
                new ProviderRatesResponse();

        providerResponse.setQuotes(quotes);

        when(providerClient.getLatestRates())
                .thenReturn(providerResponse);

        MultipleConversionResponse response =
                exchangeRateService
                        .convertAmountToMultipleCurrencies(
                                "EUR",
                                List.of(
                                        "GBP",
                                        "USD",
                                        "JPY"
                                ),
                                new BigDecimal("100")
                        );

        assertEquals(
                "EUR",
                response.from()
        );

        assertEquals(
                new BigDecimal("100"),
                response.amount()
        );

        assertEquals(
                new BigDecimal("75.0000000000"),
                response.conversions()
                        .get("GBP")
        );

        assertEquals(
                new BigDecimal("125.0000000000"),
                response.conversions()
                        .get("USD")
        );

        assertEquals(
                new BigDecimal("15000.0000000000"),
                response.conversions()
                        .get("JPY")
        );
    }

    @Test
    void shouldThrowExceptionWhenCurrencyIsUnsupported() {
        Map<String, BigDecimal> quotes =
                new HashMap<>();

        quotes.put(
                "USDEUR",
                new BigDecimal("0.80")
        );

        ProviderRatesResponse providerResponse =
                new ProviderRatesResponse();

        providerResponse.setQuotes(quotes);

        when(providerClient.getLatestRates())
                .thenReturn(providerResponse);

        UnsupportedCurrencyException exception =
                assertThrows(
                        UnsupportedCurrencyException.class,
                        () -> exchangeRateService
                                .getExchangeRate(
                                        "EUR",
                                        "XYZ"
                                )
                );

        assertEquals(
                "Unsupported currency: XYZ",
                exception.getMessage()
        );
    }
}