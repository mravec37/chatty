package com.example.chatty.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JoinResponse {
    private boolean success;
    private String userId;
    private String username;
    private String error;

    public JoinResponse(boolean success, String userId, String username, String error) {
        this.success = success;
        this.userId = userId;
        this.username = username;
        this.error = error;
    }

}