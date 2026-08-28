package com.rho.exchangerate.graphql.mapper;

import com.rho.exchangerate.exception.InvalidAmountException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class GraphqlInputParser {

    public BigDecimal parseAmount(String amount) {
        try {
            return new BigDecimal(amount);
        } catch (NumberFormatException exception) {
            throw new InvalidAmountException(
                    "Amount must be a valid number"
            );
        }
    }
}