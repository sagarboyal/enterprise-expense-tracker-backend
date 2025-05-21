package com.team7.enterpriseexpensemanagementsystem.config;

import com.team7.enterpriseexpensemanagementsystem.entity.Role;
import com.team7.enterpriseexpensemanagementsystem.entity.Roles;
import com.team7.enterpriseexpensemanagementsystem.entity.User;
import com.team7.enterpriseexpensemanagementsystem.repository.RoleRepository;
import com.team7.enterpriseexpensemanagementsystem.repository.UserRepository;
import com.team7.enterpriseexpensemanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
    @Bean
    public CommandLineRunner commandLineRunner(RoleRepository roleRepository,
                                               UserRepository userRepository,
                                               PasswordEncoder passwordEncoder) {
        return args -> {

            Role roleAdmin = roleRepository.findByRoleName(Roles.ROLE_ADMIN)
                    .orElseGet(() -> {
                        Role newUserRole = new Role(Roles.ROLE_ADMIN);
                        return roleRepository.save(newUserRole);
                    });
            Role roleManager = roleRepository.findByRoleName(Roles.ROLE_MANAGER)
                    .orElseGet(() -> {
                        Role newUserRole = new Role(Roles.ROLE_MANAGER);
                        return roleRepository.save(newUserRole);
                    });
            Role roleEmployee = roleRepository.findByRoleName(Roles.ROLE_EMPLOYEE)
                    .orElseGet(() -> {
                        Role newUserRole = new Role(Roles.ROLE_EMPLOYEE);
                        return roleRepository.save(newUserRole);
                    });

            Set<Role> adminRoles = Set.of(roleAdmin, roleManager, roleEmployee);
            Set<Role> managerRoles = Set.of(roleManager, roleEmployee);
            Set<Role> employeeRoles = Set.of(roleEmployee);

            if (!userRepository.existsByEmail("admin@gmail.com")) {
                User admin = new User("admin", "admin@gmail.com", passwordEncoder.encode("admin"));
                userRepository.save(admin);
            }
            if (!userRepository.existsByEmail("manager@gmail.com")) {
                User admin = new User("manager", "manager@gmail.com", passwordEncoder.encode("manager"));
                userRepository.save(admin);
            }
            if (!userRepository.existsByEmail("user@gmail.com")) {
                User admin = new User("user", "user@gmail.com", passwordEncoder.encode("user"));
                userRepository.save(admin);
            }

            userRepository.findByEmail("admin@gmail.com").ifPresent(admin -> {
                admin.setRoles(adminRoles);
                userRepository.save(admin);
            });
            userRepository.findByEmail("manager@gmail.com").ifPresent(manager -> {
                manager.setRoles(managerRoles);
                userRepository.save(manager);
            });
            userRepository.findByEmail("user@gmail.com").ifPresent(user -> {
                user.setRoles(employeeRoles);
                userRepository.save(user);
            });
        };
    }
}
