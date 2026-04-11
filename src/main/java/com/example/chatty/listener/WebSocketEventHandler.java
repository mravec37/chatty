package com.example.chatty.listener;

import com.example.chatty.service.ConversationService;
import com.example.chatty.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.UUID;

@Component
public class WebSocketEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventHandler.class);

    private final UserService userService;
    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventHandler(UserService userService,
                                 ConversationService conversationService,
                                 SimpMessagingTemplate messagingTemplate) {
        this.userService = userService;
        this.conversationService = conversationService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleUserDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        logger.info("WebSocket disconnect detected for sessionId={}", sessionId);

        UUID removedUserId = userService.removeBySessionId(sessionId);

        if (removedUserId == null) {
            logger.warn("No user found for sessionId={}", sessionId);
            return;
        }

        logger.info("User disconnected: userId={}", removedUserId);

        Set<UUID> partners = conversationService.getConversationPartners(removedUserId);
        sendArchiveUserNotification(partners, removedUserId);

        messagingTemplate.convertAndSend("/topic/users", userService.getAllUsers());
    }

    private void sendArchiveUserNotification(Set<UUID> partners, UUID removedUserId) {
        for (UUID partner : partners) {
            logger.debug("Sending archive notification to partner={} for removedUser={}", partner, removedUserId);

            messagingTemplate.convertAndSend(
                    "/topic/users/archived/" + partner,
                    removedUserId.toString()
            );
        }
    }
}