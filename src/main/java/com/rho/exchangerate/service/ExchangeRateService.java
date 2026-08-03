package com.rho.exchangerate.service;

import com.rho.exchangerate.client.ExchangeRateProviderClient;
import com.rho.exchangerate.dto.AllExchangeRatesResponse;
import com.rho.exchangerate.dto.ExchangeRateResponse;
import com.rho.exchangerate.dto.ProviderRatesResponse;
import com.rho.exchangerate.dto.ConversionResponse;
import com.rho.exchangerate.dto.MultipleConversionResponse;
import org.springframework.stereotype.Service;

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

    // Returns the exchange rate from USD to the requested currency.
    private BigDecimal findUsdRate(
            String currency,
            Map<String, BigDecimal> quotes) {

        // USD is the provider's base currency, so its rate is always 1.
        if (PROVIDER_BASE_CURRENCY.equals(currency)) {
            return BigDecimal.ONE;
        }

        // Builds the key used by the provider, for example "USDEUR".
        String quoteKey = PROVIDER_BASE_CURRENCY + currency;

        // Retrieves the corresponding rate from the quotes map.
        BigDecimal rate = quotes.get(quoteKey);

        if (rate == null) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + currency
            );
        }

        return rate;
    }

    //gets the exchange rates
    public ExchangeRateResponse getExchangeRate(String from, String to) {
        String normalizedFrom = from.toUpperCase(Locale.ROOT);
        String normalizedTo = to.toUpperCase(Locale.ROOT);

        ProviderRatesResponse providerResponse = providerClient.getLatestRates();
        Map<String, BigDecimal> quotes = providerResponse.getQuotes();

        BigDecimal fromRate = findUsdRate(normalizedFrom, quotes);
        BigDecimal toRate = findUsdRate(normalizedTo, quotes);

        BigDecimal rate = toRate.divide(fromRate, 10, RoundingMode.HALF_UP);

        //delivers the response in uppercase
        return new ExchangeRateResponse(
                normalizedFrom,
                normalizedTo,
                rate
        );
    }



    // Returns all exchange rates using the requested currency as the base.
    public AllExchangeRatesResponse getAllRates(String from) {

        // Normalizes the currency code, for example "eur" becomes "EUR".
        String normalizedFrom = from.toUpperCase(Locale.ROOT);

        // Gets the latest USD-based rates from the external provider.
        ProviderRatesResponse providerResponse =
                providerClient.getLatestRates();

        Map<String, BigDecimal> quotes = providerResponse.getQuotes();

        // Gets the provider rate from USD to the requested base currency.
        BigDecimal fromRate = findUsdRate(normalizedFrom, quotes);

        // TreeMap keeps the currencies ordered alphabetically.
        Map<String, BigDecimal> calculatedRates = new TreeMap<>();

        // Calculates the rate from the requested base currency to USD.
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

    // Converts an amount from one currency to another.
    public ConversionResponse convertAmount(
            String from,
            String to,
            BigDecimal amount) {

        ExchangeRateResponse exchangeRate =
                getExchangeRate(from, to);


        // Mutiplies the amount by the exchange rate to get the final value
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

    // Converts an amount from one currency to multiple target currencies.
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
                throw new IllegalArgumentException(
                        "Unsupported currency: " + normalizedTarget
                );
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