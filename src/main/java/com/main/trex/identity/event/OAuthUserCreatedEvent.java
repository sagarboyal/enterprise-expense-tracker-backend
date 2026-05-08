package com.main.trex.identity.event;

import com.main.trex.identity.entity.User;

public record OAuthUserCreatedEvent(User user, String notificationMessage) {
}