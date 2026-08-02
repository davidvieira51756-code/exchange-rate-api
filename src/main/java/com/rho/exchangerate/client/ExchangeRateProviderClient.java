package com.rho.exchangerate.client;

import com.rho.exchangerate.dto.ProviderRatesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ExchangeRateProviderClient {

    private final RestClient restClient;
    private final String accessKey;

    // Creates the client used to communicate with the external exchange-rate API.
    public ExchangeRateProviderClient(
            @Value("${exchange-rate.api.base-url}") String baseUrl,
            @Value("${exchange-rate.api.access-key}") String accessKey) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.accessKey = accessKey;
    }

    // Requests the latest exchange rates from the external provider.
    public ProviderRatesResponse getLatestRates() {
        return restClient.get()
                // Builds the /live request and adds the API key as a query parameter.
                .uri(uriBuilder -> uriBuilder
                        .path("/live")
                        .queryParam("access_key", accessKey)
                        .build())
                // Sends the request and reads the response.
                .retrieve()
                .body(ProviderRatesResponse.class);
    }
}