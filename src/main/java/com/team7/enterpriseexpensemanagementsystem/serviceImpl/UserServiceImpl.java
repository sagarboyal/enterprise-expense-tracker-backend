package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.entity.*;
import com.team7.enterpriseexpensemanagementsystem.exception.ApiException;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.payload.request.RoleUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.UserResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.*;
import com.team7.enterpriseexpensemanagementsystem.service.AuditLogService;
import com.team7.enterpriseexpensemanagementsystem.service.EmailService;
import com.team7.enterpriseexpensemanagementsystem.service.NotificationService;
import com.team7.enterpriseexpensemanagementsystem.service.UserService;
import com.team7.enterpriseexpensemanagementsystem.specification.UserSpecification;
import com.team7.enterpriseexpensemanagementsystem.utils.AuthUtils;
import com.team7.enterpriseexpensemanagementsystem.utils.ObjectMapperUtils;
import com.team7.enterpriseexpensemanagementsystem.utils.UserUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final NotificationService notificationService;
    private final ApprovalRepository approvalRepository;
    private final NotificationRepository notificationRepository;
    private final InvoiceRepository invoiceRepository;
    @Value("${frontend.url}")
    private String frontEndUrl;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;
    private final ExpenseRepository expenseRepository;
    private final AuditLogService auditLogService;
    private final AuthUtils authUtils;
    private final ObjectMapperUtils mapperUtils;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Override
    public UserResponse createUser(UserRequest request) {
        Role defaultRole = roleRepository.findByRoleName(Roles.ROLE_EMPLOYEE)
                .orElseThrow(() -> new ApiException("Invalid Role"));

        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(defaultRole));
        user = userRepository.save(user);

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(Roles.ROLE_EMPLOYEE.toString())
                .totalExpenses(BigDecimal.valueOf(0.0))
                .build();

        auditLogService.log(AuditLog.builder()
                .entityName("user")
                .entityId(user.getId())
                .action("CREATED")
                .performedBy(authUtils.loggedInEmail())
                .oldValue("")
                .newValue(mapperUtils.convertToJson(response))
                .build());

        notificationService.saveNotification(
                new Notification("Your account has been successfully created. Welcome aboard!"),
                user.getId()
        );
        return response;
    }

    @Override
    public UserResponse updateUser(UserUpdateRequest request) {
        User oldUser = userRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found with id: " + request.getId()));

        AuditLog auditLog = AuditLog.builder()
                .entityName("user")
                .entityId(oldUser.getId())
                .action("UPDATED")
                .performedBy(authUtils.loggedInEmail())
                .oldValue(mapperUtils.convertToJson(getUserById(oldUser.getId())))
                .build();

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

        auditLog.setNewValue(mapperUtils.convertToJson(getUserById(oldUser.getId())));
        auditLogService.log(auditLog);
        notificationService.saveNotification(
                new Notification("Your account has been successfully updated."),
                oldUser.getId()
        );
        return getUserById(oldUser.getId());
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        User data = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found with id: " + id));

        String oldUserJson = mapperUtils.convertToJson(getUserById(id));
        auditLogService.log(AuditLog.builder()
                .entityName("user")
                .entityId(id)
                .action("DELETED")
                .performedBy(authUtils.loggedInEmail())
                .oldValue(oldUserJson)
                .newValue(null)
                .build());

        for (Expense expense : data.getExpenses()) {
            expense.getApprovals().clear();
            expenseRepository.delete(expense);
        }


        passwordResetTokenRepository.deleteByUserId(id);
        invoiceRepository.deleteByUserId(id);

        data.getExpenses().clear();
        data.getNotifications().clear();
        userRepository.delete(data);
    }



    @Override
    public PagedResponse<UserResponse> getAllUsers(String name, String email, String role, Double minAmount, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Specification<User> specs = Specification.where(UserSpecification.hasName(name))
                .and(UserSpecification.hasEmail(email))
                .and(UserSpecification.hasRole(role))
                .and(UserSpecification.hasMinTotalExpense(minAmount));

        Sort sort = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable page = PageRequest.of(pageNumber, pageSize, sort);
        Page<User> pageResponse = userRepository.findAll(specs, page);
        List<User> users = pageResponse.getContent();

        List<UserResponse> userResponses = users.stream().map(
                user -> getUserById(user.getId())
        ).toList();

        return PagedResponse.<UserResponse>builder()
                .content(userResponses)
                .pageNumber(pageResponse.getNumber())
                .pageSize(pageResponse.getSize())
                .totalElements(pageResponse.getTotalElements())
                .totalPages(pageResponse.getTotalPages())
                .lastPage(pageResponse.isLast())
                .build();
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

        AuditLog auditLog = AuditLog.builder()
                .entityName("user")
                .entityId(user.getId())
                .performedBy(authUtils.loggedInEmail())
                .oldValue(mapperUtils.convertToJson(getUserById(user.getId())))
                .build();

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
        auditLog.setAction(action.equalsIgnoreCase("promote") ? "PROMOTED" : "DEMOTED");
        auditLog.setNewValue(mapperUtils.convertToJson(getUserById(user.getId())));
        auditLogService.log(auditLog);
        notificationService.saveNotification(
                new Notification(action.equalsIgnoreCase("promote") ?
                        "Congratulation you have been promoted.🔥":"Oops! sorry you have been demoted🥲!"),
                user.getId()
        );
        return getUserById(user.getId());
    }

    @Override
    public void generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not registered!"));

        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(24, ChronoUnit.HOURS);
        PasswordResetToken passwordResetToken =
                new PasswordResetToken(token, expiry, user);

        passwordResetTokenRepository.save(passwordResetToken);

        String resetUrl = frontEndUrl + "/reset-password?token=" + token;

        emailService.sendPasswordResetEmail(email, resetUrl);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Token not found"));

        if(resetToken.isUsed())
            throw new ApiException("Token is used");

        if(resetToken.getExpiryDate().isBefore(Instant.now()))
            throw new ApiException("Token is expired");

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
