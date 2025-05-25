package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.User;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest request);
    User updateUser(User user);
    User getUserByEmail(String email);
    User deleteUser(Long id);
    List<User> getAllUsers();
    User getUserById(Long id);
}
