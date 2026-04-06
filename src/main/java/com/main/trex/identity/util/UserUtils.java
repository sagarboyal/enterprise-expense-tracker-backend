package com.main.trex.identity.util;

import com.main.trex.identity.entity.Roles;
import com.main.trex.identity.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {
    private static boolean hasRole(User user, Roles targetRole) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == targetRole);
    }

    public static boolean isAdmin(User user) {
        return hasRole(user, Roles.ROLE_ADMIN);
    }

    public static boolean isManager(User user) {
        return hasRole(user, Roles.ROLE_MANAGER);
    }

    public static boolean isFinance(User user) {
        return hasRole(user, Roles.ROLE_FINANCE);
    }

    public static boolean isEmployee(User user) {
        return hasRole(user, Roles.ROLE_EMPLOYEE);
    }
}


