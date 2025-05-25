package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.entity.Role;
import com.team7.enterpriseexpensemanagementsystem.entity.Roles;
import com.team7.enterpriseexpensemanagementsystem.entity.User;
import com.team7.enterpriseexpensemanagementsystem.exception.ApiException;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.payload.request.RoleUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.UserResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.ExpenseRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.RoleRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.UserRepository;
import com.team7.enterpriseexpensemanagementsystem.service.UserService;
import com.team7.enterpriseexpensemanagementsystem.utils.UserUtils;
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
    private final RoleRepository roleRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public UserResponse createUser(UserRequest request) {
        Role defaultRole = roleRepository.findByRoleName(Roles.ROLE_EMPLOYEE)
                .orElseThrow(() -> new ApiException("Invalid Role"));

        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(defaultRole));
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
    public UserResponse updateUser(UserUpdateRequest request) {
        User oldUser = userRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found with id: " + request.getId()));
        String password = request.getPassword() != null && !request.getPassword().isEmpty() ? request.getPassword() : null;
        String email = request.getEmail() != null &&
                !request.getEmail().isEmpty() &&
                !request.getEmail().equalsIgnoreCase(oldUser.getEmail()) ? request.getEmail() : null;

        if(email != null && userRepository.existsByEmail(email))
            throw new ApiException("Email Already Exists");

        oldUser.setFullName(request.getFullName() != null && !request.getFullName().isEmpty() ? request.getFullName() : oldUser.getFullName());

        if(email != null)
            oldUser.setEmail(email);
        if (password != null)
            oldUser.setPassword(passwordEncoder.encode(password));

        oldUser = userRepository.save(oldUser);
        return getUserById(oldUser.getId());
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found with email: " + email));
    }

    @Override
    public User deleteUser(Long id) {
        User data = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found with id: " + id));
        userRepository.delete(data);
        return data;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user =  userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found with id: " + id));

        StringBuilder sb = new StringBuilder();
        for (Role role : user.getRoles()) {
            sb.append(role.getRoleName()).append(", ");
        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 2);
        }
        String rolesString = sb.toString();

        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByUserId(user.getId());

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(rolesString)
                .totalExpenses(totalExpenses == null ? BigDecimal.ZERO : totalExpenses)
                .build();
    }

    @Override
    public UserResponse updateRoles(Long id, RoleUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found with id: " + id));

        Role managerRole = roleRepository.findByRoleName(Roles.ROLE_MANAGER)
                .orElseThrow(() -> new ApiException("Manager Role Not Found"));
        Role adminRole = roleRepository.findByRoleName(Roles.ROLE_ADMIN)
                .orElseThrow(() -> new ApiException("Admin Role Not Found"));

        String role = request.getRole().toLowerCase();
        String action = request.getAction().toLowerCase();

        if (action.equals("promote")) {
            switch (role) {
                case "manager":
                    if(UserUtils.isEmployee(user))
                        user.getRoles().add(managerRole);
                    break;
                case "admin":
                    if (UserUtils.isEmployee(user)) {
                        user.getRoles().add(managerRole);
                        user.getRoles().add(adminRole);
                    } else if (UserUtils.isManager(user)) {
                        user.getRoles().add(adminRole);
                    }
                    break;
                default:
                    throw new ApiException("Invalid role for promotion");
            }
        } else if (action.equals("demote")) {
            switch (role) {
                case "manager":
                    if (UserUtils.isAdmin(user)) {
                        user.getRoles().remove(adminRole);
                    } else {
                        throw new ApiException("Cannot demote to manager from non-admin");
                    }
                    break;
                case "employee":
                    if (UserUtils.isManager(user)) {
                        user.getRoles().remove(managerRole);
                    }
                    if (UserUtils.isAdmin(user)) {
                        user.getRoles().remove(adminRole);
                        user.getRoles().remove(managerRole);
                    }
                    break;
                default:
                    throw new ApiException("Invalid role for demotion");
            }
        } else {
            throw new ApiException("Invalid action. Choose 'promote' or 'demote'");
        }

        user = userRepository.save(user);
        return getUserById(user.getId());
    }

}
