package com.rho.exchangerate.config;

import com.rho.exchangerate.ratelimit.RateLimitFilter;
import com.rho.exchangerate.ratelimit.RedisRateLimiter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RedisRateLimiter redisRateLimiter;
    private RateLimitFilter rateLimitFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        redisRateLimiter = mock(RedisRateLimiter.class);
        rateLimitFilter = new RateLimitFilter(redisRateLimiter);
        filterChain = mock(FilterChain.class);

        Jwt jwt = new Jwt(
                "test-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "test-subject",
                        "client_id", "exchange-rate-api"
                )
        );

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(jwt);

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowRequestWithinRateLimit() throws Exception {
        when(redisRateLimiter.tryConsume("exchange-rate-api"))
                .thenReturn(true);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        rateLimitFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(redisRateLimiter)
                .tryConsume("exchange-rate-api");

        verify(filterChain)
                .doFilter(request, response);

        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldReturn429WhenRateLimitIsExceeded() throws Exception {
        when(redisRateLimiter.tryConsume("exchange-rate-api"))
                .thenReturn(false);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        rateLimitFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertEquals(429, response.getStatus());

        assertEquals(
                "application/json",
                response.getContentType()
        );

        verify(redisRateLimiter)
                .tryConsume("exchange-rate-api");

        verify(filterChain, never())
                .doFilter(any(), any());
    }
}