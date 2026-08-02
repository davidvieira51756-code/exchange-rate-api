package com.rho.exchangerate.service;

import com.rho.exchangerate.client.ExchangeRateProviderClient;
import com.rho.exchangerate.dto.ExchangeRateResponse;
import com.rho.exchangerate.dto.ProviderRatesResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;

@Service
public class ExchangeRateService {

    private static final String PROVIDER_BASE_CURRENCY = "USD";

    private final ExchangeRateProviderClient providerClient;

    public ExchangeRateService(ExchangeRateProviderClient providerClient) {
        this.providerClient = providerClient;
    }

    public ExchangeRateResponse getExchangeRate(String from, String to) {
        String normalizedFrom = from.toUpperCase(Locale.ROOT);
        String normalizedTo = to.toUpperCase(Locale.ROOT);

        ProviderRatesResponse providerResponse = providerClient.getLatestRates();
        Map<String, BigDecimal> quotes = providerResponse.getQuotes();

        BigDecimal fromRate = findUsdRate(normalizedFrom, quotes);
        BigDecimal toRate = findUsdRate(normalizedTo, quotes);

        BigDecimal rate = toRate.divide(fromRate, 10, RoundingMode.HALF_UP);

        return new ExchangeRateResponse(
                normalizedFrom,
                normalizedTo,
                rate
        );
    }

    private BigDecimal findUsdRate(
            String currency,
            Map<String, BigDecimal> quotes) {

        if (PROVIDER_BASE_CURRENCY.equals(currency)) {
            return BigDecimal.ONE;
        }

        String quoteKey = PROVIDER_BASE_CURRENCY + currency;
        BigDecimal rate = quotes.get(quoteKey);

        if (rate == null) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + currency
            );
        }

        return rate;
    }
}