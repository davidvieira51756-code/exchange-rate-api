package com.rho.exchangerate.client;

import com.rho.exchangerate.dto.ProviderRatesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.cache.annotation.Cacheable;
import com.rho.exchangerate.exception.ExchangeRateProviderException;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.time.Duration;

@Component
public class ExchangeRateProviderClient {

    private final RestClient restClient;
    private final String accessKey;

    // Creates the client used to communicate with the external exchange-rate API.
    public ExchangeRateProviderClient(
            @Value("${exchange-rate.api.base-url}")
            String baseUrl,

            @Value("${exchange-rate.api.access-key}")
            String accessKey,

            @Value("${exchange-rate.api.connect-timeout}")
            Duration connectTimeout,

            @Value("${exchange-rate.api.read-timeout}")
            Duration readTimeout
    ) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();

        this.accessKey = accessKey;
    }

    private void validateProviderResponse(
            ProviderRatesResponse response
    ) {
        if (response == null) {
            throw new ExchangeRateProviderException(
                    "External provider returned an empty response"
            );
        }

        if (!response.isSuccess()) {
            throw new ExchangeRateProviderException(
                    "External provider returned an unsuccessful response"
            );
        }

        if (response.getQuotes() == null
                || response.getQuotes().isEmpty()) {
            throw new ExchangeRateProviderException(
                    "External provider returned no exchange rates"
            );
        }
    }

    // Requests the latest exchange rates from the external provider.
    @Cacheable(cacheNames = "latestRates", sync = true)
    public ProviderRatesResponse getLatestRates() {

        try {
            ProviderRatesResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/live")
                            .queryParam("access_key", accessKey)
                            .build())
                    .retrieve()
                    .body(ProviderRatesResponse.class);

            validateProviderResponse(response);

            return response;

        } catch (RestClientException exception) {
            throw new ExchangeRateProviderException(
                    "Unable to retrieve exchange rates from external provider",
                    exception
            );
        }
    }
}