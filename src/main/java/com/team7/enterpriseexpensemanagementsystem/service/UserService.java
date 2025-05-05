package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.User;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User updateUser(User user);
    User getUserByEmail(String email);
    User deleteUser(Long id);
    List<User> getAllUsers();
    User getUserById(Long id);
}
