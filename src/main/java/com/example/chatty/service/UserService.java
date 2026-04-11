package com.example.chatty.service;

import com.example.chatty.User;
import com.example.chatty.dto.JoinResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private final Map<UUID, User> usersById = new HashMap<>();
    private final Map<String, UUID> userIdByUsername = new HashMap<>();
    private final Map<String, UUID> userIdBySessionId = new HashMap<>();

    public synchronized JoinResponse join(String username) {
        if (username == null || username.isBlank()) {
            return new JoinResponse(false, null, null, "Username is required");
        }

        if (userIdByUsername.containsKey(username)) {
            return new JoinResponse(false, null, null, "Username is already taken");
        }

        UUID userId = UUID.randomUUID();
        User user = new User(userId, username);

        usersById.put(userId, user);
        userIdByUsername.put(username, userId);

        return new JoinResponse(true, userId.toString(), username, null);
    }

    public synchronized void bindSession(String sessionId, String userIdString) {
        UUID userId = UUID.fromString(userIdString);
        userIdBySessionId.put(sessionId, userId);
    }

    public synchronized UUID removeBySessionId(String sessionId) {
        UUID userId = userIdBySessionId.remove(sessionId);

        if (userId == null) {
            return null;
        }

        User removed = usersById.remove(userId);
        if (removed != null) {
            userIdByUsername.remove(removed.getUsername());
        }

        return userId;
    }

    public synchronized boolean existsById(String userIdString) {
        try {
            UUID userId = UUID.fromString(userIdString);
            return usersById.containsKey(userId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public synchronized List<User> getAllUsers() {
        return new ArrayList<>(usersById.values());
    }
}