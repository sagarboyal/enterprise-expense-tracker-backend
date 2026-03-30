package com.main.trex.identity.service;

import com.main.trex.identity.payload.request.RoleUpdateRequest;
import com.main.trex.identity.payload.request.UserRequest;
import com.main.trex.identity.payload.request.UserUpdateRequest;
import com.main.trex.shared.payload.response.PagedResponse;
import com.main.trex.identity.payload.response.UserResponse;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(UserUpdateRequest request);
    void deleteUser(Long id);
    PagedResponse<UserResponse> getAllUsers(String name, String email, String role, Double minAmount, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    UserResponse getUserById(Long id);
    UserResponse updateRoles(Long id, RoleUpdateRequest request);

    void generatePasswordResetToken(String email);

    void resetPassword(String token, String newPassword);
}


