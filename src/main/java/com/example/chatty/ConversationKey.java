package com.example.chatty;

import java.util.Objects;
import java.util.UUID;

/*
* Class that allows us to find conversation of two users in HashMap by making the two user id combination always produce the same hash output
*  */
public final class ConversationKey {

    private final UUID firstUserId;
    private final UUID secondUserId;

    public ConversationKey(UUID userA, UUID userB) {
        if (userA.toString().compareTo(userB.toString()) <= 0) {
            this.firstUserId = userA;
            this.secondUserId = userB;
        } else {
            this.firstUserId = userB;
            this.secondUserId = userA;
        }
    }

    public UUID getFirstUserId() {
        return firstUserId;
    }

    public UUID getSecondUserId() {
        return secondUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversationKey)) return false;
        ConversationKey other = (ConversationKey) o;
        return firstUserId.equals(other.firstUserId)
                && secondUserId.equals(other.secondUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstUserId, secondUserId);
    }
}
