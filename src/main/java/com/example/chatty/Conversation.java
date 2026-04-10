package com.example.chatty;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Conversation {
    private UUID firstUserId;
    private UUID secondUserId;
    private List<ChatMessage> messages = new ArrayList<>();

    public Conversation(UUID firstUserId, UUID secondUserId) {
        this.firstUserId = firstUserId;
        this.secondUserId = secondUserId;
    }

}
