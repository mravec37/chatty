package com.example.chatty.controller;

import com.example.chatty.dto.JoinRequest;
import com.example.chatty.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    public ChatController(SimpMessagingTemplate messagingTemplate, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    @MessageMapping("/connect")
    public void connect(String userId) {
        System.out.println(userId);

        messagingTemplate.convertAndSend("/topic/users", userService.getAllUsers());
    }


}
