package com.example.chatty.service;

import com.example.chatty.model.ChatMessage;
import com.example.chatty.model.Conversation;
import com.example.chatty.model.ConversationKey;
import com.example.chatty.dto.ChatMessageDto;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ConversationService {

    private static final DateTimeFormatter MESSAGE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Map<ConversationKey, Conversation> conversations = new HashMap<>();
    private final Map<UUID, Set<UUID>> conversationPartners = new HashMap<>();

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

    public synchronized List<ChatMessageDto> getConversationMessages(String userId, String targetUserId) {
        UUID firstId = UUID.fromString(userId);
        UUID secondId = UUID.fromString(targetUserId);

        Conversation conversation = conversations.get(new ConversationKey(firstId, secondId));

        if (conversation == null) {
            return List.of();
        }

        List<ChatMessageDto> result = new ArrayList<>();
        for (ChatMessage message : conversation.getMessages()) {
            result.add(toDto(message));
        }

        return result;
    }

    private void registerConversationPartners(UUID firstUserId, UUID secondUserId) {
        Set<UUID> firstUserPartners = conversationPartners.get(firstUserId);
        if (firstUserPartners == null) {
            firstUserPartners = new HashSet<>();
            conversationPartners.put(firstUserId, firstUserPartners);
        }
        firstUserPartners.add(secondUserId);

        Set<UUID> secondUserPartners = conversationPartners.get(secondUserId);
        if (secondUserPartners == null) {
            secondUserPartners = new HashSet<>();
            conversationPartners.put(secondUserId, secondUserPartners);
        }
        secondUserPartners.add(firstUserId);
    }

    public synchronized Set<UUID> getConversationPartners(UUID userId) {
        Set<UUID> partners = conversationPartners.get(userId);

        if (partners == null) {
            return Set.of();
        }

        return new HashSet<>(partners);
    }

    public synchronized void addMessage(ChatMessage message) {
        UUID fromUserId = UUID.fromString(message.getFromUserId());
        UUID toUserId = UUID.fromString(message.getToUserId());

        ConversationKey key = new ConversationKey(fromUserId, toUserId);

        Conversation conversation = conversations.get(key);
        if (conversation == null) {
            conversation = new Conversation(fromUserId, toUserId);
            conversations.put(key, conversation);
        }

        conversation.getMessages().add(message);
        registerConversationPartners(fromUserId, toUserId);
    }
}