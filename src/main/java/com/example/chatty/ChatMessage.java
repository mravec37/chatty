package com.example.chatty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatMessage {

    private String fromUserId;
    private String toUserId;
    private String content;
    private LocalDateTime sentAt;

}
