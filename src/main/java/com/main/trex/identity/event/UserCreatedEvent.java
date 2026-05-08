package com.main.trex.identity.event;

import com.main.trex.identity.entity.Roles;
import com.main.trex.identity.entity.User;

public record UserCreatedEvent(
        User user,
        Roles role,
        String notificationMessage,
        String clientIp
) {}