package com.team7.enterpriseexpensemanagementsystem.utils;

import com.team7.enterpriseexpensemanagementsystem.entity.Roles;
import com.team7.enterpriseexpensemanagementsystem.entity.User;
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

    public static boolean isEmployee(User user) {
        return hasRole(user, Roles.ROLE_EMPLOYEE);
    }
}
