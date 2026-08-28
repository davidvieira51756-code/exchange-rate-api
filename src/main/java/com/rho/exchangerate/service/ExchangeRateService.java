package com.rho.exchangerate.service;

import com.rho.exchangerate.client.ExchangeRateProviderClient;
import com.rho.exchangerate.dto.AllExchangeRatesResponse;
import com.rho.exchangerate.dto.ConversionResponse;
import com.rho.exchangerate.dto.ExchangeRateResponse;
import com.rho.exchangerate.dto.MultipleConversionResponse;
import com.rho.exchangerate.dto.ProviderRatesResponse;
import com.rho.exchangerate.exception.InvalidAmountException;
import com.rho.exchangerate.exception.UnsupportedCurrencyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ExchangeRateService {

    private static final String PROVIDER_BASE_CURRENCY = "USD";

    private final ExchangeRateProviderClient providerClient;

    public ExchangeRateService(ExchangeRateProviderClient providerClient) {
        this.providerClient = providerClient;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Amount must be greater than zero"
            );
        }
    }

    private BigDecimal findUsdRate(
            String currency,
            Map<String, BigDecimal> quotes
    ) {
        if (PROVIDER_BASE_CURRENCY.equals(currency)) {
            return BigDecimal.ONE;
        }

        String quoteKey = PROVIDER_BASE_CURRENCY + currency;

        BigDecimal rate = quotes.get(quoteKey);

        if (rate == null) {
            throw new UnsupportedCurrencyException(currency);
        }

        return rate;
    }

    public ExchangeRateResponse getExchangeRate(String from, String to) {
        String normalizedFrom = from.toUpperCase(Locale.ROOT);
        String normalizedTo = to.toUpperCase(Locale.ROOT);

        ProviderRatesResponse providerResponse =
                providerClient.getLatestRates();

        Map<String, BigDecimal> quotes =
                providerResponse.getQuotes();

        BigDecimal fromRate =
                findUsdRate(normalizedFrom, quotes);

        BigDecimal toRate =
                findUsdRate(normalizedTo, quotes);

        BigDecimal rate = toRate.divide(
                fromRate,
                10,
                RoundingMode.HALF_UP
        );

        return new ExchangeRateResponse(
                normalizedFrom,
                normalizedTo,
                rate
        );
    }

    public AllExchangeRatesResponse getAllRates(String from) {
        String normalizedFrom =
                from.toUpperCase(Locale.ROOT);

        ProviderRatesResponse providerResponse =
                providerClient.getLatestRates();

        Map<String, BigDecimal> quotes =
                providerResponse.getQuotes();

        BigDecimal fromRate =
                findUsdRate(normalizedFrom, quotes);

        Map<String, BigDecimal> calculatedRates =
                new TreeMap<>();

        calculatedRates.put(
                PROVIDER_BASE_CURRENCY,
                BigDecimal.ONE.divide(
                        fromRate,
                        10,
                        RoundingMode.HALF_UP
                )
        );

        for (Map.Entry<String, BigDecimal> entry
                : quotes.entrySet()) {

            String quoteKey = entry.getKey();

            if (!quoteKey.startsWith(PROVIDER_BASE_CURRENCY)
                    || quoteKey.length() != 6) {
                continue;
            }

            String targetCurrency =
                    quoteKey.substring(3);

            BigDecimal targetRate =
                    entry.getValue();

            BigDecimal calculatedRate =
                    targetRate.divide(
                            fromRate,
                            10,
                            RoundingMode.HALF_UP
                    );

            calculatedRates.put(
                    targetCurrency,
                    calculatedRate
            );
        }

        calculatedRates.put(
                normalizedFrom,
                BigDecimal.ONE
        );

        return new AllExchangeRatesResponse(
                normalizedFrom,
                calculatedRates
        );
    }

    public ConversionResponse convertAmount(
            String from,
            String to,
            BigDecimal amount
    ) {
        validateAmount(amount);

        ExchangeRateResponse exchangeRate =
                getExchangeRate(from, to);

        BigDecimal convertedAmount = amount
                .multiply(exchangeRate.rate())
                .setScale(
                        10,
                        RoundingMode.HALF_UP
                );

        return new ConversionResponse(
                exchangeRate.from(),
                exchangeRate.to(),
                amount,
                exchangeRate.rate(),
                convertedAmount
        );
    }

    public MultipleConversionResponse convertAmountToMultipleCurrencies(
            String from,
            List<String> targetCurrencies,
            BigDecimal amount
    ) {
        validateAmount(amount);

        AllExchangeRatesResponse allRates =
                getAllRates(from);

        Map<String, BigDecimal> conversions =
                new TreeMap<>();

        for (String targetCurrency : targetCurrencies) {
            String normalizedTarget =
                    targetCurrency.toUpperCase(Locale.ROOT);

            BigDecimal rate =
                    allRates.rates().get(normalizedTarget);

            if (rate == null) {
                throw new UnsupportedCurrencyException(
                        normalizedTarget
                );
            }

            BigDecimal convertedAmount = amount
                    .multiply(rate)
                    .setScale(
                            10,
                            RoundingMode.HALF_UP
                    );

            conversions.put(
                    normalizedTarget,
                    convertedAmount
            );
        }

        return new MultipleConversionResponse(
                allRates.base(),
                amount,
                conversions
        );
    }
}