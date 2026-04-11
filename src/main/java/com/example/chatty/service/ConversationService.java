package com.example.chatty.service;



import com.example.chatty.ChatMessage;
import com.example.chatty.Conversation;
import com.example.chatty.ConversationKey;
import com.example.chatty.dto.ChatMessageDto;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationService {

    private static final DateTimeFormatter MESSAGE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ConcurrentHashMap<ConversationKey, Conversation> conversations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Set<UUID>> conversationPartners = new ConcurrentHashMap<>();

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

        List<ChatMessageDto> result = new ArrayList<>();
        for (ChatMessage message : conversation.getMessages()) {
            result.add(toDto(message));
        }

        return result;
    }

    private void registerConversationPartners(UUID firstUserId, UUID secondUserId) {
        Set<UUID> firstUserPartners = conversationPartners.get(firstUserId);
        if (firstUserPartners == null) {
            firstUserPartners = ConcurrentHashMap.newKeySet();
            conversationPartners.put(firstUserId, firstUserPartners);
        }
        firstUserPartners.add(secondUserId);

        Set<UUID> secondUserPartners = conversationPartners.get(secondUserId);
        if (secondUserPartners == null) {
            secondUserPartners = ConcurrentHashMap.newKeySet();
            conversationPartners.put(secondUserId, secondUserPartners);
        }
        secondUserPartners.add(firstUserId);
    }

    public Set<UUID> getConversationPartners(UUID userId) {
        Set<UUID> partners = conversationPartners.get(userId);

        if (partners == null) {
            return Set.of();
        }

        return partners;
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
        registerConversationPartners(fromUserId, toUserId);
    }

    public boolean conversationExists(String userId, String targetUserId) {
        UUID firstId = UUID.fromString(userId);
        UUID secondId = UUID.fromString(targetUserId);

        return conversations.containsKey(new ConversationKey(firstId, secondId));
    }
}