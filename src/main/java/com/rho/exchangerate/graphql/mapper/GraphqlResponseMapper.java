package com.rho.exchangerate.graphql.mapper;

import com.rho.exchangerate.dto.AllExchangeRatesResponse;
import com.rho.exchangerate.dto.MultipleConversionResponse;
import com.rho.exchangerate.graphql.dto.ConversionEntry;
import com.rho.exchangerate.graphql.dto.GraphqlAllRatesResponse;
import com.rho.exchangerate.graphql.dto.GraphqlMultipleConversionResponse;
import com.rho.exchangerate.graphql.dto.RateEntry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GraphqlResponseMapper {

    public GraphqlAllRatesResponse toGraphqlAllRatesResponse(
            AllExchangeRatesResponse response
    ) {
        List<RateEntry> rates = response.rates()
                .entrySet()
                .stream()
                .map(entry -> new RateEntry(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();

        return new GraphqlAllRatesResponse(
                response.base(),
                rates
        );
    }

    public GraphqlMultipleConversionResponse toGraphqlMultipleConversionResponse(
            MultipleConversionResponse response
    ) {
        List<ConversionEntry> conversions =
                response.conversions()
                        .entrySet()
                        .stream()
                        .map(entry -> new ConversionEntry(
                                entry.getKey(),
                                entry.getValue()
                        ))
                        .toList();

        return new GraphqlMultipleConversionResponse(
                response.from(),
                response.amount(),
                conversions
        );
    }
}