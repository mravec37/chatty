package com.example.chatty.service;


import com.example.chatty.dto.JoinResponse;
import com.example.chatty.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    void join_shouldRejectDuplicateUsername() {
        JoinResponse first = userService.join("tom");
        JoinResponse second = userService.join("tom");

        assertTrue(first.isSuccess());
        assertFalse(second.isSuccess());
        assertEquals("Username is already taken", second.getError());
    }

    @Test
    void removeBySessionId_shouldRemoveUserAndFreeUsername() {
        JoinResponse joinResponse = userService.join("tom");
        assertTrue(joinResponse.isSuccess());

        String userId = joinResponse.getUserId();
        String sessionId = "session-123";

        userService.bindSession(sessionId, userId);

        UUID removedUserId = userService.removeBySessionId(sessionId);

        assertNotNull(removedUserId);
        assertEquals(UUID.fromString(userId), removedUserId);
        assertFalse(userService.existsById(userId));

        JoinResponse secondJoin = userService.join("tom");
        assertTrue(secondJoin.isSuccess());
    }
}