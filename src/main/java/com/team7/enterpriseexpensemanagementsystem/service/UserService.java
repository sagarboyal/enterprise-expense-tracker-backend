package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.payload.request.RoleUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.UserResponse;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(UserUpdateRequest request);
    void deleteUser(Long id);
    PagedResponse<UserResponse> getAllUsers(String name, String email, String role, Double minAmount, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    UserResponse getUserById(Long id);
    UserResponse updateRoles(Long id, RoleUpdateRequest request);
}
