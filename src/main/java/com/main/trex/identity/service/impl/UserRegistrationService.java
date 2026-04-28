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

import java.math.BigDecimal;
import java.util.Set;

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

    public UserResponse createPersonalUser(UserRequest request) {
        Role defaultRole = roleRepository.findByRoleName(Roles.ROLE_EMPLOYEE)
                .orElseThrow(() -> new ApiException("Invalid Role"));

        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setProvider(AuthProvider.EMAIL);
        user.setRoles(Set.of(defaultRole));

        PersonalUser profile = new PersonalUser();
        user.setPersonalProfile(profile);
        profile.setUser(user);

        user = userRepository.save(user);

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(Roles.ROLE_USER.toString())
                .build();

        auditLogService.log(AuditLog.builder()
                .entityName("user")
                .entityId(user.getId())
                .action("CREATED")
                .performedBy(user.getEmail())
                .oldValue("")
                .newValue(mapperUtils.convertToJson(response))
                .build());

        notificationService.saveNotification(
                new Notification("Your account has been successfully created. Welcome aboard!"),
                user.getId()
        );
        return response;
    }
}
