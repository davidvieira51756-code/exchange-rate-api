package com.rho.exchangerate.client;

import com.rho.exchangerate.dto.ProviderRatesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ExchangeRateProviderClient {

    private final RestClient restClient;
    private final String accessKey;

    public ExchangeRateProviderClient(
            @Value("${exchange-rate.api.base-url}") String baseUrl,
            @Value("${exchange-rate.api.access-key}") String accessKey) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.accessKey = accessKey;
    }

    public ProviderRatesResponse getLatestRates() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/live")
                        .queryParam("access_key", accessKey)
                        .build())
                .retrieve()
                .body(ProviderRatesResponse.class);
    }
}