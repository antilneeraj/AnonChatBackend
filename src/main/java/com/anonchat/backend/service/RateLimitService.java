package com.anonchat.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    // CONFIG: Allow 5 messages every 10 seconds
    private static final int MAX_MESSAGES = 5;
    private static final int TIME_WINDOW = 10;

    public boolean isRateLimited(String sessionId) {
        String key = "rate:limit:" + sessionId;

        // Increment the counter (returns the new value)
        Long count = redisTemplate.opsForValue().increment(key);

        // If it's the first message (count == 1), start the expiration timer
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(TIME_WINDOW));
        }

        // Check if they exceeded the limit
        return count != null && count > MAX_MESSAGES;
    }
}