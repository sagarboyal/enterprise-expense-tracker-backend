package com.main.trex.identity.service.impl;

import com.main.trex.identity.entity.*;
import com.main.trex.identity.payload.request.UserRequest;
import com.main.trex.identity.payload.response.UserResponse;
import com.main.trex.identity.repository.RoleRepository;
import com.main.trex.identity.repository.UserRepository;
import com.main.trex.notification.entity.Notification;
import com.main.trex.notification.service.NotificationService;
import com.main.trex.shared.exception.ApiException;
import com.main.trex.shared.util.ObjectMapperUtils;
import com.main.trex.support.audit.entity.AuditLog;
import com.main.trex.support.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapperUtils mapperUtils;

    @Transactional
    public UserResponse createPersonalUser(UserRequest request) {

        // 1. check duplicate email
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ApiException("Email already registered");

        // 2. fetch default role for personal users
        Role defaultRole = roleRepository.findByRoleName(Roles.ROLE_USER)
                .orElseThrow(() -> new ApiException("Default role not found"));

        // 3. build User
        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProvider(AuthProvider.EMAIL);
        user.setUserType(UserType.PERSONAL);
        user.setActiveContext(UserType.PERSONAL);
        user.setIsEmailVerified(true);  // personal users skip email verification
        user.setEnabled(true);
        user.setRoles(Set.of(defaultRole));

        // 4. build PersonalUser profile and link BEFORE save
        // CascadeType.ALL on personalProfile will persist it automatically
        PersonalUser profile = new PersonalUser();
        profile.setUser(user);
        user.setPersonalProfile(profile);

        // 5. save — cascade saves PersonalUser too
        user = userRepository.save(user);

        // 6. build response
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(Roles.ROLE_USER.name())
                .build();

        // 7. audit log
        auditLogService.log(AuditLog.builder()
                .entityName("user")
                .entityId(user.getId())
                .action("CREATED")
                .performedBy(user.getEmail())
                .oldValue("")
                .newValue(mapperUtils.convertToJson(response))
                .build());

        // 8. welcome notification
        notificationService.saveNotification(
                new Notification("Your account has been successfully created. Welcome aboard!"),
                user.getId()
        );

        return response;
    }

    @Transactional
    public User findOrCreateOAuthUser(String email, String name, String providerId) {
        return userRepository.findByEmail(email)
                .map(existingUser -> syncOAuthUser(existingUser, name))
                .orElseGet(() -> createOAuthUser(email, name, providerId));
    }

    private User createOAuthUser(String email, String name, String providerId) {
        Role userRole = roleRepository.findByRoleName(Roles.ROLE_USER)
                .orElseThrow(() -> new ApiException("Default user role not found."));

        User user = new User();
        user.setEmail(email);
        user.setFullName(name != null && !name.isBlank() ? name : email);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setProvider(AuthProvider.GOOGLE);
        user.setUserType(UserType.PERSONAL);
        user.setActiveContext(UserType.PERSONAL);
        user.setIsEmailVerified(true);
        user.setEnabled(true);
        user.setRoles(Set.of(userRole));

        PersonalUser profile = new PersonalUser();
        profile.setUser(user);
        profile.setGoogleId(providerId);
        user.setPersonalProfile(profile);

        user = userRepository.save(user);

        auditLogService.log(AuditLog.builder()
                .entityName("user")
                .entityId(user.getId())
                .action("CREATED_OAUTH")
                .performedBy(user.getEmail())
                .oldValue("")
                .newValue(mapperUtils.convertToJson(user))
                .build());

        notificationService.saveNotification(
                new Notification("Your Google account has been linked and your personal workspace is ready."),
                user.getId()
        );

        return user;
    }

    private User syncOAuthUser(User user, String name) {
        if (user.getProvider() == AuthProvider.EMAIL) {
            throw new ApiException(
                    "This email is already registered with a password. Please log in with email and password. " +
                            "You can link your Google account from account settings."
            );
        }
        if (name != null && !name.isBlank()) {
            user.setFullName(name);
        }
        return userRepository.save(user);
    }
}