package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.User;
import com.team7.enterpriseexpensemanagementsystem.payload.request.RoleUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(UserUpdateRequest request);
    User getUserByEmail(String email);
    User deleteUser(Long id);
    List<User> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateRoles(Long id, RoleUpdateRequest request);
}
