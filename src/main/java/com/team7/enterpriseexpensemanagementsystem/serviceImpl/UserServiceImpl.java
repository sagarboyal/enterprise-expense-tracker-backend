package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.entity.User;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.repository.UserRepository;
import com.team7.enterpriseexpensemanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        User oldUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        oldUser.setEmail(user.getEmail() != null ? user.getEmail() : oldUser.getEmail());
        oldUser.setFullName(user.getFullName() != null ? user.getFullName() : oldUser.getFullName());
        oldUser.setPassword(user.getPassword() != null ? user.getPassword() : oldUser.getPassword());

        return userRepository.save(oldUser);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found with email: " + email));
    }

    @Override
    public User deleteUser(Long id) {
        User data = getUserById(id);
        userRepository.delete(data);
        return data;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found with id: " + id));
    }
}
