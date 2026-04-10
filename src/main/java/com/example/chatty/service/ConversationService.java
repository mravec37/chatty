package com.example.chatty.service;

import com.example.chatty.ChatMessage;
import com.example.chatty.Conversation;
import com.example.chatty.ConversationKey;
import com.example.chatty.dto.ChatMessageDto;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationService {

    private static final DateTimeFormatter MESSAGE_TIME_FORMAT =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private ConcurrentHashMap<ConversationKey, Conversation> conversations = new ConcurrentHashMap<>();

    public static ChatMessageDto toDto(ChatMessage message) {
        String formattedSentAt = "";
        if (message.getSentAt() != null) {
            formattedSentAt = message.getSentAt().format(MESSAGE_TIME_FORMAT);
        }

        return new ChatMessageDto(
                message.getFromUserId(),
                message.getToUserId(),
                message.getContent(),
                formattedSentAt
        );
    }

    public List<ChatMessageDto> getConversationMessages(String userId, String targetUserId) {
        UUID firstId = UUID.fromString(userId);
        UUID secondId = UUID.fromString(targetUserId);

        Conversation conversation = conversations.get(new ConversationKey(firstId, secondId));

        if (conversation == null) {
            return List.of();
        }

        return conversation.getMessages()
                .stream()
                .map(ConversationService::toDto)
                .toList();
    }

    public void addMessage(ChatMessage message) {
        UUID fromUserId = UUID.fromString(message.getFromUserId());
        UUID toUserId = UUID.fromString(message.getToUserId());

        ConversationKey key = new ConversationKey(fromUserId, toUserId);

        Conversation conversation = conversations.get(key);

        if (conversation == null) {
            conversation = new Conversation(fromUserId, toUserId);
            conversations.put(key, conversation);
        }

        conversation.getMessages().add(message);
    }
}
