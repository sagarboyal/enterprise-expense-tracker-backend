package com.team7.enterpriseexpensemanagementsystem.repository;

import com.team7.enterpriseexpensemanagementsystem.entity.Role;
import com.team7.enterpriseexpensemanagementsystem.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(Roles roleName);
}
