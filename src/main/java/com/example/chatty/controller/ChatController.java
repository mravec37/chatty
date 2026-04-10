package com.example.chatty.controller;

import com.example.chatty.ChatMessage;
import com.example.chatty.dto.ChatMessageDto;
import com.example.chatty.dto.ConnectRequest;
import com.example.chatty.service.ConversationService;
import com.example.chatty.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final ConversationService conversationService;

    public ChatController(SimpMessagingTemplate messagingTemplate,
                          UserService userService,
                          ConversationService conversationService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.conversationService = conversationService;
    }

    @MessageMapping("/connect")
    public void connect(ConnectRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String userId = request.getUserId();
        String sessionId = headerAccessor.getSessionId();

        System.out.println("userId: " + userId);
        System.out.println("Websocket session id: " + sessionId);

        if (userId == null || userId.isBlank()) {
            return;
        }

        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        if (!userService.existsById(userId)) {
            return;
        }

        userService.bindSession(sessionId, userId);
        messagingTemplate.convertAndSend("/topic/users", userService.getAllUsers());
    }
    @MessageMapping("/chat")
    public void chat(ChatMessage message) {
        if (!isValidMessage(message)) return;

        message.setSentAt(LocalDateTime.now());

        conversationService.addMessage(message);

        ChatMessageDto dto = ConversationService.toDto(message);

        messagingTemplate.convertAndSend("/topic/messages/" + message.getToUserId(), dto);
        messagingTemplate.convertAndSend("/topic/messages/" + message.getFromUserId(), dto);
    }

    private boolean isValidMessage(ChatMessage message) {
        if (message == null) {
            return false;
        }

        if (message.getFromUserId() == null || message.getFromUserId().isBlank()) {
            return false;
        }

        if (message.getToUserId() == null || message.getToUserId().isBlank()) {
            return false;
        }

        if (message.getContent() == null || message.getContent().isBlank()) {
            return false;
        }

        if (!userService.existsById(message.getFromUserId())) {
            return false;
        }

        if (!userService.existsById(message.getToUserId())) {
            return false;
        }
        return true;
    }

}
