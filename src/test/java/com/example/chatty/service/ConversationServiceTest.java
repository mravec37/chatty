package com.example.chatty.service;


import com.example.chatty.model.ChatMessage;
import com.example.chatty.dto.ChatMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConversationServiceTest {

    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        conversationService = new ConversationService();
    }


    @Test
    void getConversationMessages_shouldReturnStoredMessages() {
        String userA = UUID.randomUUID().toString();
        String userB = UUID.randomUUID().toString();

        ChatMessage first = new ChatMessage();
        first.setFromUserId(userA);
        first.setToUserId(userB);
        first.setContent("Hello");
        first.setSentAt(LocalDateTime.of(2026, 4, 11, 10, 30));

        ChatMessage second = new ChatMessage();
        second.setFromUserId(userB);
        second.setToUserId(userA);
        second.setContent("Hi");
        second.setSentAt(LocalDateTime.of(2026, 4, 11, 10, 31));

        conversationService.addMessage(first);
        conversationService.addMessage(second);

        List<ChatMessageDto> result = conversationService.getConversationMessages(userA, userB);

        assertEquals(2, result.size());

        assertEquals(userA, result.get(0).getFromUserId());
        assertEquals(userB, result.get(0).getToUserId());
        assertEquals("Hello", result.get(0).getContent());
        assertEquals("2026-04-11 10:30", result.get(0).getSentAt());

        assertEquals(userB, result.get(1).getFromUserId());
        assertEquals(userA, result.get(1).getToUserId());
        assertEquals("Hi", result.get(1).getContent());
        assertEquals("2026-04-11 10:31", result.get(1).getSentAt());
    }
}
