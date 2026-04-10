package com.example.chatty.service;


import com.example.chatty.User;
import com.example.chatty.dto.JoinResponse;
import org.springframework.stereotype.Service;

import java.util.*;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final Map<String, User> usersById = new ConcurrentHashMap<>();
    private final Map<String, String> userIdByUsername = new ConcurrentHashMap<>();

    public synchronized JoinResponse join(String username) {
        if (username == null || username.isBlank()) {
            return new JoinResponse(false, null, null, "Username is required");
        }

        if (userIdByUsername.containsKey(username)) {
            return new JoinResponse(false, null, null, "Username already taken");
        }

        String userId = UUID.randomUUID().toString();
        User user = new User(userId, username);

        usersById.put(userId, user);
        userIdByUsername.put(username, userId);

        return new JoinResponse(true, userId, username, null);
    }

    public void removeById(String userId) {
        User user = usersById.remove(userId);
        if (user != null) {
            userIdByUsername.remove(user.getUsername());
        }
    }

    public boolean existsById(String userId) {
        return usersById.containsKey(userId);
    }

    public User getById(String userId) {
        return usersById.get(userId);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(usersById.values());
    }
}