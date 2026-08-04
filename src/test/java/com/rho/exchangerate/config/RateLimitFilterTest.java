package com.rho.exchangerate.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
        filterChain = mock(FilterChain.class);
    }

    @Test
    void shouldAllowRequestsWithinRateLimit() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRemoteAddr("192.168.1.10");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        rateLimitFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain, times(1))
                .doFilter(request, response);

        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldReturn429WhenRateLimitIsExceeded() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRemoteAddr("192.168.1.20");

        for (int i = 0; i < 60; i++) {
            MockHttpServletResponse allowedResponse =
                    new MockHttpServletResponse();

            rateLimitFilter.doFilter(
                    request,
                    allowedResponse,
                    filterChain
            );
        }

        MockHttpServletResponse blockedResponse =
                new MockHttpServletResponse();

        rateLimitFilter.doFilter(
                request,
                blockedResponse,
                filterChain
        );

        assertEquals(429, blockedResponse.getStatus());
        assertEquals(
                "application/json",
                blockedResponse.getContentType()
        );

        verify(filterChain, times(60))
                .doFilter(
                        eq(request),
                        any(MockHttpServletResponse.class)
                );
    }
}