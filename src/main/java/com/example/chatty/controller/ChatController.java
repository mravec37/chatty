package com.example.chatty.controller;

import com.example.chatty.model.ChatMessage;
import com.example.chatty.dto.ChatMessageDto;
import com.example.chatty.dto.ConnectRequest;
import com.example.chatty.service.ConversationService;
import com.example.chatty.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

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

        logger.info("User attempting to connect: userId={}, sessionId={}", userId, sessionId);

        if (userId == null || userId.isBlank()) {
            logger.warn("Connect rejected: userId is null or blank");
            return;
        }

        if (sessionId == null || sessionId.isBlank()) {
            logger.warn("Connect rejected: sessionId is null or blank");
            return;
        }

        if (!userService.existsById(userId)) {
            logger.warn("Connect rejected: user does not exist, userId={}", userId);
            return;
        }

        userService.bindSession(sessionId, userId);

        logger.info("User connected successfully: userId={}", userId);

        messagingTemplate.convertAndSend("/topic/users", userService.getAllUsers());
    }

    @MessageMapping("/chat")
    public void chat(ChatMessage message) {
        if (!isValidMessage(message)) {
            logger.warn("Invalid message received: {}", message);
            return;
        }

        message.setSentAt(LocalDateTime.now());

        conversationService.addMessage(message);

        ChatMessageDto dto = ConversationService.toDto(message);

        logger.info("Message sent from {} to {}", message.getFromUserId(), message.getToUserId());

        messagingTemplate.convertAndSend("/topic/messages/" + message.getToUserId(), dto);
        messagingTemplate.convertAndSend("/topic/messages/" + message.getFromUserId(), dto);
    }

    private boolean isValidMessage(ChatMessage message) {
        if (message == null) {
            logger.warn("Validation failed: message is null");
            return false;
        }

        if (message.getFromUserId() == null || message.getFromUserId().isBlank()) {
            logger.warn("Validation failed: fromUserId is invalid");
            return false;
        }

        if (message.getToUserId() == null || message.getToUserId().isBlank()) {
            logger.warn("Validation failed: toUserId is invalid");
            return false;
        }

        if (message.getContent() == null || message.getContent().isBlank()) {
            logger.warn("Validation failed: message content is empty");
            return false;
        }

        if (!userService.existsById(message.getFromUserId())) {
            logger.warn("Validation failed: sender does not exist: {}", message.getFromUserId());
            return false;
        }

        if (!userService.existsById(message.getToUserId())) {
            logger.warn("Validation failed: receiver does not exist: {}", message.getToUserId());
            return false;
        }

        return true;
    }
}