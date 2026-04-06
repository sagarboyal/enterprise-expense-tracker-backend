package com.main.trex.organization.service.impl;

import com.main.trex.identity.entity.User;
import com.main.trex.identity.repository.UserRepository;
import com.main.trex.notification.entity.Notification;
import com.main.trex.notification.service.NotificationService;
import com.main.trex.organization.entity.Organization;
import com.main.trex.organization.entity.OrganizationInvite;
import com.main.trex.organization.entity.OrganizationInviteStatus;
import com.main.trex.organization.entity.OrganizationMember;
import com.main.trex.organization.entity.OrganizationMemberStatus;
import com.main.trex.organization.entity.OrganizationRole;
import com.main.trex.organization.payload.request.CreateOrganizationRequest;
import com.main.trex.organization.payload.request.OrganizationInviteRequest;
import com.main.trex.organization.payload.response.OrganizationInviteResponse;
import com.main.trex.organization.payload.response.OrganizationMemberResponse;
import com.main.trex.organization.payload.response.OrganizationResponse;
import com.main.trex.organization.repository.OrganizationInviteRepository;
import com.main.trex.organization.repository.OrganizationMemberRepository;
import com.main.trex.organization.repository.OrganizationRepository;
import com.main.trex.organization.service.OrganizationService;
import com.main.trex.shared.exception.ApiException;
import com.main.trex.shared.exception.ResourceNotFoundException;
import com.main.trex.support.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationInviteRepository organizationInviteRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public OrganizationResponse registerOrganization(CreateOrganizationRequest request, String creatorEmail) {
        if (organizationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ApiException("Organization name already exists.");
        }

        User creator = getUserByEmail(creatorEmail);
        Organization organization = new Organization();
        organization.setName(request.getName().trim());
        organization.setDescription(request.getDescription());
        organization.setCreatedBy(creator);
        organization.setCreatedAt(LocalDateTime.now());
        organization.setActive(true);
        organization = organizationRepository.save(organization);

        OrganizationMember member = new OrganizationMember();
        member.setOrganization(organization);
        member.setUser(creator);
        member.setRole(OrganizationRole.ORG_ADMIN);
        member.setStatus(OrganizationMemberStatus.ACTIVE);
        member.setJoinedAt(LocalDateTime.now());
        organizationMemberRepository.save(member);

        notificationService.saveNotification(
                new Notification("Organization '" + organization.getName() + "' was created successfully."),
                creator.getId()
        );

        return toResponse(organization);
    }

    @Override
    @Transactional
    public OrganizationInviteResponse inviteMember(Long organizationId, OrganizationInviteRequest request, String inviterEmail) {
        User inviter = getUserByEmail(inviterEmail);
        Organization organization = getOrganization(organizationId);
        OrganizationMember inviterMembership = getActiveMembership(organizationId, inviter.getId());

        if (inviterMembership.getRole() != OrganizationRole.ORG_ADMIN) {
            throw new ApiException("Only organization admins can invite members.");
        }

        if (organizationInviteRepository.existsByOrganizationIdAndInvitedEmailIgnoreCaseAndStatus(
                organizationId,
                request.getEmail(),
                OrganizationInviteStatus.PENDING)
        ) {
            throw new ApiException("There is already a pending invite for this email.");
        }

        userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
            if (organizationMemberRepository.existsByOrganizationIdAndUserIdAndStatus(
                    organizationId,
                    existingUser.getId(),
                    OrganizationMemberStatus.ACTIVE)
            ) {
                throw new ApiException("User is already an active member of this organization.");
            }
        });

        OrganizationInvite invite = new OrganizationInvite();
        invite.setOrganization(organization);
        invite.setInvitedEmail(request.getEmail().trim().toLowerCase());
        invite.setRole(request.getRole());
        invite.setToken(UUID.randomUUID().toString());
        invite.setInvitedBy(inviter);
        invite.setStatus(OrganizationInviteStatus.PENDING);
        invite.setCreatedAt(LocalDateTime.now());
        invite.setExpiresAt(LocalDateTime.now().plusDays(7));
        invite = organizationInviteRepository.save(invite);

        String acceptUrl = frontendUrl + "/organization/invitations/accept?token=" + invite.getToken();
        emailService.sendOrganizationInviteEmail(
                invite.getInvitedEmail(),
                organization.getName(),
                request.getRole().name(),
                acceptUrl
        );

        return toInviteResponse(invite);
    }

    @Override
    @Transactional
    public OrganizationInviteResponse acceptInvite(String token, String currentUserEmail) {
        OrganizationInvite invite = organizationInviteRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found."));

        if (invite.getStatus() != OrganizationInviteStatus.PENDING) {
            throw new ApiException("Invitation is no longer valid.");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus(OrganizationInviteStatus.EXPIRED);
            organizationInviteRepository.save(invite);
            throw new ApiException("Invitation has expired.");
        }

        User currentUser = getUserByEmail(currentUserEmail);
        if (!invite.getInvitedEmail().equalsIgnoreCase(currentUser.getEmail())) {
            throw new ApiException("This invitation does not belong to the logged-in user.");
        }

        Organization organization = invite.getOrganization();
        OrganizationMember membership = organizationMemberRepository
                .findByOrganizationIdAndUserId(organization.getId(), currentUser.getId())
                .orElseGet(OrganizationMember::new);

        membership.setOrganization(organization);
        membership.setUser(currentUser);
        membership.setRole(invite.getRole());
        membership.setStatus(OrganizationMemberStatus.ACTIVE);
        membership.setJoinedAt(LocalDateTime.now());
        organizationMemberRepository.save(membership);

        invite.setStatus(OrganizationInviteStatus.ACCEPTED);
        organizationInviteRepository.save(invite);

        notificationService.saveNotification(
                new Notification("You joined organization '" + organization.getName() + "' as " + invite.getRole().name() + "."),
                currentUser.getId()
        );

        return toInviteResponse(invite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationMemberResponse> getMembers(Long organizationId, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        getActiveMembership(organizationId, requester.getId());

        return organizationMemberRepository.findAllByOrganizationId(organizationId).stream()
                .map(member -> OrganizationMemberResponse.builder()
                        .userId(member.getUser().getId())
                        .fullName(member.getUser().getFullName())
                        .email(member.getUser().getEmail())
                        .role(member.getRole())
                        .status(member.getStatus())
                        .joinedAt(member.getJoinedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponse> getMyOrganizations(String userEmail) {
        User user = getUserByEmail(userEmail);
        return organizationMemberRepository.findAllByUserIdAndStatus(user.getId(), OrganizationMemberStatus.ACTIVE).stream()
                .map(OrganizationMember::getOrganization)
                .map(this::toResponse)
                .toList();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private Organization getOrganization(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found."));
    }

    private OrganizationMember getActiveMembership(Long organizationId, Long userId) {
        OrganizationMember membership = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new ApiException("User is not a member of this organization."));
        if (membership.getStatus() != OrganizationMemberStatus.ACTIVE) {
            throw new ApiException("Organization membership is not active.");
        }
        return membership;
    }

    private OrganizationResponse toResponse(Organization organization) {
        int activeMembers = (int) organizationMemberRepository.findAllByOrganizationId(organization.getId()).stream()
                .filter(member -> member.getStatus() == OrganizationMemberStatus.ACTIVE)
                .count();

        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .createdBy(organization.getCreatedBy().getId())
                .createdAt(organization.getCreatedAt())
                .active(organization.isActive())
                .activeMembers(activeMembers)
                .build();
    }

    private OrganizationInviteResponse toInviteResponse(OrganizationInvite invite) {
        return OrganizationInviteResponse.builder()
                .inviteId(invite.getId())
                .organizationId(invite.getOrganization().getId())
                .organizationName(invite.getOrganization().getName())
                .invitedEmail(invite.getInvitedEmail())
                .role(invite.getRole())
                .token(invite.getToken())
                .expiresAt(invite.getExpiresAt())
                .status(invite.getStatus())
                .build();
    }
}
