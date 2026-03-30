package com.main.trex.identity.repository;

import com.main.trex.identity.entity.Role;
import com.main.trex.identity.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(Roles roleName);
}


