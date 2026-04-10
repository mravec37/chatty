package com.example.chatty.controller;

import com.example.chatty.ChatMessage;
import com.example.chatty.dto.ChatMessageDto;
import com.example.chatty.service.ConversationService;
import com.example.chatty.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
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
    public void connect(String userId) {
        System.out.println(userId);

        messagingTemplate.convertAndSend("/topic/users", userService.getAllUsers());
    }
    @MessageMapping("/chat")
    public void chat(ChatMessage message) {
        if (!validMessage(message)) return;

        message.setSentAt(LocalDateTime.now());

        conversationService.addMessage(message);

        ChatMessageDto dto = ConversationService.toDto(message);

        messagingTemplate.convertAndSend("/topic/messages/" + message.getToUserId(), dto);
        messagingTemplate.convertAndSend("/topic/messages/" + message.getFromUserId(), dto);
    }

    private boolean validMessage(ChatMessage message) {
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
