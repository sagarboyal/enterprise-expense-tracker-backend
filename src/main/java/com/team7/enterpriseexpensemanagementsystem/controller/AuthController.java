package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.entity.Notification;
import com.team7.enterpriseexpensemanagementsystem.entity.Roles;
import com.team7.enterpriseexpensemanagementsystem.entity.User;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.jwt.JwtUtils;
import com.team7.enterpriseexpensemanagementsystem.payload.request.SignInRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.request.SignUpRequest;
import com.team7.enterpriseexpensemanagementsystem.payload.response.MessageResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.SignInResponse;
import com.team7.enterpriseexpensemanagementsystem.payload.response.UserInfoResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.RoleRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.UserRepository;
import com.team7.enterpriseexpensemanagementsystem.service.NotificationService;
import com.team7.enterpriseexpensemanagementsystem.service.UserService;
import com.team7.enterpriseexpensemanagementsystem.utils.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtils authUtils;
    private final UserService userService;
    private final NotificationService notificationService;

    @PostMapping("/public/sign-in")
    public ResponseEntity<?> authenticateUser(@RequestBody SignInRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("error", exception.getMessage());
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);


        return ResponseEntity.ok(SignInResponse.builder()
                .username(userDetails.getUsername())
                .token(jwtToken)
                .build());
    }

    @PostMapping("/public/sign-up")
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = User.builder()
                .fullName(signUpRequest.getFullName())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .roles(Set.of(roleRepository.findByRoleName(Roles.ROLE_EMPLOYEE)
                        .orElseThrow(() -> new ResourceNotFoundException("Error: Role does not exist!"))))
                .build();
        userRepository.save(user);

        notificationService.saveNotification(
                new Notification("Your account has been successfully created. Welcome aboard!"),
                user.getId()
        );

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/csrf")
    public ResponseEntity<CsrfToken> csrfHandler(HttpServletRequest request) {
        return ResponseEntity.ok((CsrfToken) request.getAttribute(CsrfToken.class.getName()));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authUtils.loggedInUser();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                roles
        );

        return ResponseEntity.ok().body(response);
    }
    @PostMapping("/public/forgot-password")
    public ResponseEntity<?> forgetPassword(@RequestParam String email) {
        try {
            userService.generatePasswordResetToken(email);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid email!"));
        }
        return ResponseEntity.ok(new MessageResponse("Password reset token generated successfully!"));
    }

    @PostMapping("/public/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
                                           @RequestParam String newPassword) {
        try {
            userService.resetPassword(token, newPassword);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid token!"));
        }
        return ResponseEntity.ok(new MessageResponse("Password reset successfully!"));
    }
}
