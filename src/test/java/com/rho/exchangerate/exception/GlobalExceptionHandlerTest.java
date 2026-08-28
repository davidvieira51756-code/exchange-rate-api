package com.rho.exchangerate.exception;

import com.rho.exchangerate.dto.ApiErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldReturnBadRequestForIllegalArgumentException() {
        IllegalArgumentException exception =
                new IllegalArgumentException("Amount must be greater than zero");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleIllegalArgumentException(exception);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals(
                "Amount must be greater than zero",
                response.getBody().getMessage()
        );
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldReturnBadGatewayForProviderException() {
        ExchangeRateProviderException exception =
                new ExchangeRateProviderException("Provider failed");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleExchangeRateProviderException(exception);

        assertEquals(502, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(502, response.getBody().getStatus());
        assertEquals("Bad Gateway", response.getBody().getError());
        assertEquals("Provider failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldReturnBadRequestForInvalidAmountType() {
        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException(
                        "abc",
                        BigDecimal.class,
                        "amount",
                        null,
                        new NumberFormatException("abc")
                );

        ResponseEntity<ApiErrorResponse> response =
                handler.handleMethodArgumentTypeMismatch(exception);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals(
                "Invalid value for request parameter: amount",
                response.getBody().getMessage()
        );
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldReturnBadRequestForMissingRequiredParameter() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException(
                        "amount",
                        "BigDecimal"
                );

        ResponseEntity<ApiErrorResponse> response =
                handler.handleMissingServletRequestParameter(exception);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals(
                "Missing required request parameter: amount",
                response.getBody().getMessage()
        );
        assertNotNull(response.getBody().getTimestamp());
    }
}