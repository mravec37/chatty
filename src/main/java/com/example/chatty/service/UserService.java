package com.example.chatty.service;


import com.example.chatty.User;
import com.example.chatty.dto.JoinResponse;
import org.springframework.stereotype.Service;

import java.util.*;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final Map<UUID, User> usersById = new ConcurrentHashMap<>();
    private final Map<String, UUID> userIdByUsername = new ConcurrentHashMap<>();
    private final Map<String, UUID> userIdBySessionId = new ConcurrentHashMap<>();

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

    public void removeById(UUID userId) {
        User user = usersById.remove(userId);
        if (user != null) {
            userIdByUsername.remove(user.getUsername());
        }
    }

    public void bindSession(String sessionId, String userIdString) {
        UUID userId = UUID.fromString(userIdString);
        userIdBySessionId.put(sessionId, userId);
    }

    public UUID removeBySessionId(String sessionId) {
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

    public boolean existsById(String userIdString) {
        try {
            UUID userId = UUID.fromString(userIdString);
            return usersById.containsKey(userId);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public User getById(String userIdString) {
        try {
            UUID userId = UUID.fromString(userIdString);
            return usersById.get(userId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(usersById.values());
    }
}