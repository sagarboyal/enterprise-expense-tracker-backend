package com.main.trex.identity.service.impl;

import com.main.trex.identity.entity.*;
import com.main.trex.identity.event.OAuthUserCreatedEvent;
import com.main.trex.identity.event.UserCreatedEvent;
import com.main.trex.identity.event.UserEventListener;
import com.main.trex.identity.payload.request.BusinessUserRequest;
import com.main.trex.identity.payload.request.UserRequest;
import com.main.trex.identity.payload.response.UserResponse;
import com.main.trex.identity.repository.RoleRepository;
import com.main.trex.identity.repository.UserRepository;
import com.main.trex.organization.entity.Organization;
import com.main.trex.organization.repository.OrganizationRepository;
import com.main.trex.shared.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserEventListener userEventListener;
    private final OrganizationRepository organizationRepository;
    private final HttpServletRequest httpServletRequest;

    @Transactional
    public UserResponse createPersonalUser(UserRequest request) {

        if (userRepository.existsByEmailAndUserType(request.getEmail(), UserType.PERSONAL))
            throw ApiException.conflict("A personal account is already registered with this email");

        Role userRole = roleRepository.findByRoleName(Roles.ROLE_USER)
                .orElseThrow(() -> ApiException.notFound("Default role not found"));

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            user = User.builder()
                    .email(request.getEmail())
                    .fullName(request.getFullName())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .provider(AuthProvider.EMAIL)
                    .userType(UserType.PERSONAL)
                    .activeContext(UserType.PERSONAL)
                    .isEmailVerified(true)
                    .enabled(true)
                    .roles(new HashSet<>(Set.of(userRole)))
                    .build();
        } else {
            user.setFullName(request.getFullName());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setActiveContext(UserType.PERSONAL);
            user.getRoles().add(userRole);
        }

        PersonalUser profile = new PersonalUser();
        profile.setUser(user);
        user.setPersonalProfile(profile);

        User savedUser = userRepository.save(user);

        userEventListener.handleUserCreated(new UserCreatedEvent(
                savedUser,
                Roles.ROLE_USER,
                "Your personal account has been successfully created. Welcome aboard!",
                resolveClientIp()
        ));

        return toResponse(savedUser);
    }

    @Transactional
    public UserResponse createBusinessUser(BusinessUserRequest request) {

        if (userRepository.existsByEmailAndUserType(request.email(), UserType.BUSINESS))
            throw ApiException.conflict("A business account is already registered with this email");

        Role adminRole = roleRepository.findByRoleName(Roles.ROLE_ADMIN)
                .orElseThrow(() -> ApiException.notFound("Default role not found"));

        User user = userRepository.findByEmail(request.email()).orElse(null);

        if (user == null) {
            user = User.builder()
                    .email(request.email())
                    .fullName(request.fullName())
                    .password(passwordEncoder.encode(request.password()))
                    .provider(AuthProvider.EMAIL)
                    .userType(UserType.BUSINESS)
                    .activeContext(UserType.BUSINESS)
                    .isEmailVerified(false)
                    .enabled(true)
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .build();
        } else {
            user.setFullName(request.fullName());
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setActiveContext(UserType.BUSINESS);
            user.getRoles().add(adminRole);
        }

        Organization org = Organization.builder()
                .name(request.organizationName())
                .slug(generateUniqueSlug(request.organizationName()))
                .industry(Organization.Industry.OTHER)
                .build();

        BusinessUser profile = BusinessUser.builder()
                .user(user)
                .organization(org)
                .build();

        org.setCreatedBy(profile);
        user.setBusinessProfile(profile);

        User savedUser = userRepository.save(user);

        userEventListener.handleUserCreated(new UserCreatedEvent(
                savedUser,
                Roles.ROLE_ADMIN,
                "Your business account has been successfully created. Welcome aboard!",
                resolveClientIp()
        ));

        return toResponse(savedUser);
    }

    @Transactional
    public User findOrCreateOAuthUser(String email, String name, String providerId) {
        return userRepository.findByEmail(email)
                .map(existingUser -> syncOAuthUser(existingUser, name))
                .orElseGet(() -> createOAuthUser(email, name, providerId));
    }

    private User createOAuthUser(String email, String name, String providerId) {
        Role userRole = roleRepository.findByRoleName(Roles.ROLE_USER)
                .orElseThrow(() -> new ApiException("Default user role not found."));

        User user = User.builder()
                .email(email)
                .fullName(name != null && !name.isBlank() ? name : email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .provider(AuthProvider.GOOGLE)
                .userType(UserType.PERSONAL)
                .activeContext(UserType.PERSONAL)
                .isEmailVerified(true)
                .enabled(true)
                .roles(Set.of(userRole))
                .build();

        PersonalUser profile = new PersonalUser();
        profile.setUser(user);
        profile.setGoogleId(providerId);
        user.setPersonalProfile(profile);

        User savedUser = userRepository.save(user);

        userEventListener.handleOAuthUserCreated(new OAuthUserCreatedEvent(
                savedUser,
                "Your Google account has been linked and your personal workspace is ready.",
                resolveClientIp()
        ));

        return savedUser;
    }

    private User syncOAuthUser(User user, String name) {
        if (user.getProvider() == AuthProvider.EMAIL) {
            throw new ApiException(
                    "This email is already registered with a password. Please log in with email and password. " +
                            "You can link your Google account from account settings."
            );
        }
        if (name != null && !name.isBlank()) {
            user.setFullName(name);
        }
        return userRepository.save(user);
    }

    private String generateUniqueSlug(String name) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
        String slug = base;
        int counter = 1;
        while (organizationRepository.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }

    /**
     * Resolves client IP from the current request thread.
     * Must be called before handing off to @Async — the thread-bound
     * request is not available in the async event listener.
     */
    private String resolveClientIp() {
        String forwarded = httpServletRequest.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : httpServletRequest.getRemoteAddr();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRoles().toString())
                .build();
    }
}