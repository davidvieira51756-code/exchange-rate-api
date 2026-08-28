package com.rho.exchangerate.service;

import com.rho.exchangerate.client.ExchangeRateProviderClient;
import com.rho.exchangerate.dto.AllExchangeRatesResponse;
import com.rho.exchangerate.dto.ExchangeRateResponse;
import com.rho.exchangerate.dto.ProviderRatesResponse;
import com.rho.exchangerate.dto.ConversionResponse;
import com.rho.exchangerate.dto.MultipleConversionResponse;
import org.springframework.stereotype.Service;
import com.rho.exchangerate.exception.UnsupportedCurrencyException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;

@Service
public class ExchangeRateService
{

    private static final String PROVIDER_BASE_CURRENCY = "USD";

    private final ExchangeRateProviderClient providerClient;

    public ExchangeRateService(ExchangeRateProviderClient providerClient) {
        this.providerClient = providerClient;
    }


    private BigDecimal findUsdRate(
            String currency,
            Map<String, BigDecimal> quotes) {

        // USD is the provider's base currency, so its rate is always 1.
        if (PROVIDER_BASE_CURRENCY.equals(currency)) {
            return BigDecimal.ONE;
        }

        // Builds the key used by the provider, for example "USDEUR".
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

        ProviderRatesResponse providerResponse = providerClient.getLatestRates();
        Map<String, BigDecimal> quotes = providerResponse.getQuotes();

        BigDecimal fromRate = findUsdRate(normalizedFrom, quotes);
        BigDecimal toRate = findUsdRate(normalizedTo, quotes);

        // Cross rate: from -> to = (USD -> to) / (USD -> from).
        BigDecimal rate = toRate.divide(fromRate, 10, RoundingMode.HALF_UP);

        return new ExchangeRateResponse(
                normalizedFrom,
                normalizedTo,
                rate
        );
    }



    // Returns all exchange rates using the requested currency as the base.
    public AllExchangeRatesResponse getAllRates(String from) {

        String normalizedFrom = from.toUpperCase(Locale.ROOT);

        ProviderRatesResponse providerResponse =
                providerClient.getLatestRates();

        Map<String, BigDecimal> quotes = providerResponse.getQuotes();

        BigDecimal fromRate = findUsdRate(normalizedFrom, quotes);

        // TreeMap keeps the currencies ordered alphabetically.
        Map<String, BigDecimal> calculatedRates = new TreeMap<>();

        // Since fromRate represents USD -> from, its inverse represents from -> USD.
        calculatedRates.put(
                PROVIDER_BASE_CURRENCY,
                BigDecimal.ONE.divide(fromRate, 10, RoundingMode.HALF_UP)
        );

        // Converts every USD-based provider rate to the requested base currency.
        for (Map.Entry<String, BigDecimal> entry : quotes.entrySet()) {

            String quoteKey = entry.getKey();

            // Ignores invalid quote keys.
            if (!quoteKey.startsWith(PROVIDER_BASE_CURRENCY)
                    || quoteKey.length() != 6) {
                continue;
            }

            // Extracts the target currency from keys such as "USDGBP".
            String targetCurrency = quoteKey.substring(3);

            BigDecimal targetRate = entry.getValue();

            // Example: EUR -> GBP = USD -> GBP / USD -> EUR.
            BigDecimal calculatedRate =
                    targetRate.divide(fromRate, 10, RoundingMode.HALF_UP);

            calculatedRates.put(targetCurrency, calculatedRate);
        }

        // A currency converted to itself always has a rate of 1.
        calculatedRates.put(normalizedFrom, BigDecimal.ONE);

        return new AllExchangeRatesResponse(
                normalizedFrom,
                calculatedRates
        );
    }

    public ConversionResponse convertAmount(
            String from,
            String to,
            BigDecimal amount) {

        ExchangeRateResponse exchangeRate =
                getExchangeRate(from, to);


        BigDecimal convertedAmount = amount
                .multiply(exchangeRate.getRate())
                .setScale(10, RoundingMode.HALF_UP);

        return new ConversionResponse(
                exchangeRate.getFrom(),
                exchangeRate.getTo(),
                amount,
                exchangeRate.getRate(),
                convertedAmount
        );
    }

    public MultipleConversionResponse convertAmountToMultipleCurrencies(
            String from,
            List<String> targetCurrencies,
            BigDecimal amount) {

        AllExchangeRatesResponse allRates = getAllRates(from);

        Map<String, BigDecimal> conversions = new TreeMap<>();

        for (String targetCurrency : targetCurrencies) {

            String normalizedTarget =
                    targetCurrency.toUpperCase(Locale.ROOT);

            BigDecimal rate =
                    allRates.getRates().get(normalizedTarget);

            if (rate == null) {
                throw new UnsupportedCurrencyException(normalizedTarget);
            }

            BigDecimal convertedAmount = amount
                    .multiply(rate)
                    .setScale(10, RoundingMode.HALF_UP);

            conversions.put(
                    normalizedTarget,
                    convertedAmount
            );
        }

        return new MultipleConversionResponse(
                allRates.getBase(),
                amount,
                conversions
        );
    }
}