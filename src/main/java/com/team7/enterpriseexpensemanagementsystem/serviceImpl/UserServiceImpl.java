package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.entity.Role;
import com.team7.enterpriseexpensemanagementsystem.entity.Roles;
import com.team7.enterpriseexpensemanagementsystem.entity.User;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.UserResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.UserRepository;
import com.team7.enterpriseexpensemanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    public UserResponse createUser(UserRequest request) {
        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(new Role(Roles.ROLE_EMPLOYEE)));
        user = userRepository.save(user);
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(Roles.ROLE_EMPLOYEE.toString())
                .totalExpenses(BigDecimal.valueOf(0.0))
                .build();
    }

    @Override
    public User updateUser(User user) {
        User oldUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        oldUser.setEmail(user.getEmail() != null ? user.getEmail() : oldUser.getEmail());
        oldUser.setFullName(user.getFullName() != null ? user.getFullName() : oldUser.getFullName());

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
