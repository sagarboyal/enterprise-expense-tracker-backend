package com.main.trex.organization.repository;

import com.main.trex.organization.entity.OrganizationMember;
import com.main.trex.organization.entity.OrganizationMemberStatus;
import com.main.trex.organization.entity.OrganizationRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {
    Optional<OrganizationMember> findByOrganizationIdAndUserId(Long organizationId, Long userId);
    boolean existsByOrganizationIdAndUserId(Long organizationId, Long userId);
    boolean existsByOrganizationIdAndUserIdAndStatus(Long organizationId, Long userId, OrganizationMemberStatus status);
    List<OrganizationMember> findAllByOrganizationId(Long organizationId);
    List<OrganizationMember> findAllByUserIdAndStatus(Long userId, OrganizationMemberStatus status);
    List<OrganizationMember> findAllByOrganizationIdAndRoleAndStatus(Long organizationId, OrganizationRole role, OrganizationMemberStatus status);
}
