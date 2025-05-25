package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.entity.User;
import com.team7.enterpriseexpensemanagementsystem.payload.request.RoleUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.UserUpdateRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.UserResponse;
import com.team7.enterpriseexpensemanagementsystem.service.UserService;
import com.team7.enterpriseexpensemanagementsystem.utils.AuthUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getUserById() {
        return ResponseEntity.ok(userService.getUserById(authUtils.loggedInUser().getId()));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }
    @PutMapping
    public ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{id}/roles")
    public ResponseEntity<UserResponse> updateUserRoles(@PathVariable Long id,
                                                        @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(userService.updateRoles(id, request));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

}
