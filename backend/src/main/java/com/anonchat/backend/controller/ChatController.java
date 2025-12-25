package com.anonchat.backend.controller;

import com.anonchat.backend.model.ChatMessage;
import com.anonchat.backend.service.ChatService;
import com.anonchat.backend.service.RateLimitService;
import com.anonchat.backend.service.FilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final RateLimitService rateLimitService;
    private final FilterService filterService;

    // Handling Chat Messages
    @MessageMapping("/chat/{roomId}/sendMessage")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId,
                                   @Payload ChatMessage chatMessage,
                                   SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();

        if (rateLimitService.isRateLimited(sessionId)) {
            log.warn("Rate limit exceeded for session: {}", sessionId);
            return ChatMessage.builder()
                    .type(ChatMessage.MessageType.CHAT)
                    .sender("System")
                    .content("🚫 You are typing too fast! Please wait a moment.")
                    .build();
        }

        // CONTENT CHECK: Profanity Filtering
        String cleanContent = filterService.sanitize(chatMessage.getContent());
        chatMessage.setContent(cleanContent);

        chatService.saveMessage(roomId, chatMessage);

        return chatMessage;
    }

    // Handling Join Events
    @MessageMapping("/chat/{roomId}/addUser")
    @SendTo("/topic/{roomId}")
    public ChatMessage addUser(@DestinationVariable String roomId,
                               @Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        // Sanitize username too! (Prevent people from joining as "Idiot")
        String cleanSender = filterService.sanitize(chatMessage.getSender());
        chatMessage.setSender(cleanSender);

        // Adding username AND roomId in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        return chatMessage;
    }
}