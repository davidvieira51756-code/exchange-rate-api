package com.rho.exchangerate.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class RedisRateLimiter {

    private static final int REQUESTS_PER_MINUTE = 60;
    private static final long WINDOW_SECONDS = 60;

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local current = redis.call('INCR', KEYS[1])

                    if current == 1 then
                        redis.call('EXPIRE', KEYS[1], ARGV[1])
                    end

                    if current > tonumber(ARGV[2]) then
                        return 0
                    end

                    return 1
                    """,
                    Long.class
            );

    private final StringRedisTemplate redisTemplate;

    public RedisRateLimiter(
            StringRedisTemplate redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryConsume(String clientId) {
        String key = "rate-limit:" + clientId;

        Long result = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(WINDOW_SECONDS),
                String.valueOf(REQUESTS_PER_MINUTE)
        );

        return result != null && result == 1L;
    }
}