package com.rho.exchangerate.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisRateLimiter redisRateLimiter;

    public RateLimitFilter(
            RedisRateLimiter redisRateLimiter
    ) {
        this.redisRateLimiter = redisRateLimiter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = jwtAuthentication
                .getToken()
                .getClaimAsString("client_id");

        if (clientId == null) {
            clientId = jwtAuthentication
                    .getToken()
                    .getClaimAsString("azp");
        }

        if (clientId == null) {
            clientId = jwtAuthentication
                    .getToken()
                    .getSubject();
        }

        if (redisRateLimiter.tryConsume(clientId)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(
                HttpStatus.TOO_MANY_REQUESTS.value()
        );

        response.setContentType("application/json");

        response.getWriter().write("""
                {
                  "status": 429,
                  "error": "Too Many Requests",
                  "message": "Rate limit exceeded. Maximum 60 requests per minute."
                }
                """);
    }
}