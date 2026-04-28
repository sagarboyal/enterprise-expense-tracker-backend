package com.main.trex.identity.service.impl;

import com.main.trex.identity.payload.request.RoleUpdateRequest;
import com.main.trex.identity.payload.request.UserRequest;
import com.main.trex.identity.payload.request.UserUpdateRequest;
import com.main.trex.identity.payload.response.UserResponse;
import com.main.trex.identity.service.UserService;
import com.main.trex.shared.payload.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRegistrationService userRegistrationService;
    private final UserProfileService userProfileService;
    private final UserDeletionService userDeletionService;
    private final UserQueryService userQueryService;
    private final UserRoleService userRoleService;
    private final PasswordResetService passwordResetService;

    @Override
    public UserResponse createUser(UserRequest request) {
        return userRegistrationService.createPersonalUser(request);
    }

    @Override
    public UserResponse updateUser(UserUpdateRequest request) {
        return userProfileService.updateUser(request);
    }

    @Override
    public void deleteUser(Long id) {
        userDeletionService.deleteUser(id);
    }

    @Override
    public PagedResponse<UserResponse> getAllUsers(String name, String email, String role, Double minAmount,
                                                   Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        return userQueryService.getAllUsers(name, email, role, minAmount, pageNumber, pageSize, sortBy, sortOrder);
    }

    @Override
    public UserResponse getUserById(Long id) {
        return userQueryService.getUserById(id);
    }

    @Override
    public UserResponse updateRoles(Long id, RoleUpdateRequest request) {
        return userRoleService.updateRoles(id, request);
    }

    @Override
    public void generatePasswordResetToken(String email) {
        passwordResetService.generatePasswordResetToken(email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        passwordResetService.resetPassword(token, newPassword);
    }
}
