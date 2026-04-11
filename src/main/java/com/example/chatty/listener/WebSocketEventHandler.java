package com.example.chatty.listener;

import com.example.chatty.service.ConversationService;
import com.example.chatty.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.UUID;

@Component
public class WebSocketEventHandler {

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
        System.out.println("Disconnect event for session id: " + sessionId);
        UUID removedUserId = userService.removeBySessionId(sessionId);

        var partners = conversationService.getConversationPartners(removedUserId);
        sendArchiveUserNotification(partners, removedUserId);

        if (removedUserId != null) {
            messagingTemplate.convertAndSend("/topic/users", userService.getAllUsers());
        }

    }

    private void sendArchiveUserNotification(Set<UUID> partners, UUID removedUserId) {
        partners.forEach(partner -> messagingTemplate.convertAndSend("/topic/users/archived/" + partner, removedUserId.toString()));
    }
}
