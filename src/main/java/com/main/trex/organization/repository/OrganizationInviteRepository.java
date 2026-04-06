package com.main.trex.organization.repository;

import com.main.trex.organization.entity.OrganizationInvite;
import com.main.trex.organization.entity.OrganizationInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationInviteRepository extends JpaRepository<OrganizationInvite, Long> {
    Optional<OrganizationInvite> findByToken(String token);
    List<OrganizationInvite> findAllByOrganizationIdAndStatus(Long organizationId, OrganizationInviteStatus status);
    boolean existsByOrganizationIdAndInvitedEmailIgnoreCaseAndStatus(Long organizationId, String invitedEmail, OrganizationInviteStatus status);
}
