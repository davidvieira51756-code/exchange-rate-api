package com.rho.exchangerate.graphql.exception;

import com.rho.exchangerate.exception.ExchangeRateProviderException;
import com.rho.exchangerate.exception.InvalidAmountException;
import com.rho.exchangerate.exception.InvalidCurrencyException;
import com.rho.exchangerate.exception.UnsupportedCurrencyException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GraphqlExceptionHandler
        extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(
            Throwable exception,
            DataFetchingEnvironment environment
    ) {

        if (exception instanceof ConstraintViolationException validationException) {
            String message = validationException
                    .getConstraintViolations()
                    .stream()
                    .map(violation -> violation.getMessage())
                    .findFirst()
                    .orElse("Invalid request");

            return GraphqlErrorBuilder.newError(environment)
                    .message(message)
                    .errorType(ErrorType.BAD_REQUEST)
                    .extensions(Map.of(
                            "status", 400,
                            "error", "Bad Request"
                    ))
                    .build();
        }

        if (exception instanceof InvalidCurrencyException
                || exception instanceof InvalidAmountException
                || exception instanceof UnsupportedCurrencyException) {

            return GraphqlErrorBuilder.newError(environment)
                    .message(exception.getMessage())
                    .errorType(ErrorType.BAD_REQUEST)
                    .extensions(Map.of(
                            "status", 400,
                            "error", "Bad Request"
                    ))
                    .build();
        }

        if (exception instanceof ExchangeRateProviderException) {
            return GraphqlErrorBuilder.newError(environment)
                    .message(exception.getMessage())
                    .errorType(ErrorType.INTERNAL_ERROR)
                    .extensions(Map.of(
                            "status", 502,
                            "error", "Bad Gateway"
                    ))
                    .build();
        }

        return null;
    }
}