package com.rho.exchangerate.graphql.exception;

import com.rho.exchangerate.exception.ExchangeRateProviderException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class GraphqlExceptionHandler
        extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(
            Throwable exception,
            DataFetchingEnvironment environment
    ) {
        if (exception instanceof IllegalArgumentException) {
            return GraphqlErrorBuilder.newError(environment)
                    .message(exception.getMessage())
                    .errorType(ErrorType.BAD_REQUEST)
                    .build();
        }

        if (exception instanceof ExchangeRateProviderException) {
            return GraphqlErrorBuilder.newError(environment)
                    .message(exception.getMessage())
                    .errorType(ErrorType.INTERNAL_ERROR)
                    .build();
        }

        return null;
    }
}