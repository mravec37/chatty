package com.example.chatty.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatMessageDto {

    private String fromUserId;
    private String toUserId;
    private String content;
    private String sentAt;
}
