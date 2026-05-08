package com.main.trex.organization.repository;

import com.main.trex.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    boolean existsBySlug(String slug);
    boolean existsByNameIgnoreCase(String name);
    Optional<Organization> findByNameIgnoreCase(String name);
}
