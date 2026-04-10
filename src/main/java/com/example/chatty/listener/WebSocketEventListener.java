package com.example.chatty.listener;

import com.example.chatty.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(UserService userService,
                                  SimpMessagingTemplate messagingTemplate) {
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        System.out.println("Disconnect event for session id: " + sessionId);
        String removedUserId = userService.removeBySessionId(sessionId);

        if (removedUserId != null) {
            messagingTemplate.convertAndSend("/topic/users", userService.getAllUsers());
        }
    }
}
