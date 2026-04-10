package com.example.chatty.controller;

import com.example.chatty.dto.JoinRequest;
import com.example.chatty.dto.JoinResponse;
import com.example.chatty.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JoinChatController {

    private final UserService userService;

    public JoinChatController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/join")
    public JoinResponse join(@RequestBody JoinRequest request) {
        return userService.join(request.getUsername());
    }
}