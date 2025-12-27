package com.anonchat.backend.service;

import com.anonchat.backend.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RedisTemplate<String, Object> redisTemplate;

    // CONSTANT: Maximum messages to keep in memory per room
    private static final int HISTORY_LIMIT = 1000;

    public void saveMessage(String roomId, ChatMessage message) {
        String key = "room:" + roomId;
        redisTemplate.opsForList().rightPush(key, message);

        // If size > 1000, remove the 'left' (oldest) item.
        redisTemplate.opsForList().trim(key, -HISTORY_LIMIT, -1);

        // TTL: 1 hour
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    public List<Object> getHistory(String roomId) {
        String key = "room:" + roomId;

        return redisTemplate.opsForList().range(key, 0, -1);
    }
}
