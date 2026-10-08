package com.shopease.config;

import com.shopease.entity.Role;
import com.shopease.entity.User;
import com.shopease.enums.RoleType;
import com.shopease.repository.RoleRepository;
import com.shopease.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Creates the first admin account on startup (only if it doesn't exist yet). */
@Slf4j
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedAdmin(UserRepository userRepository,
                                       RoleRepository roleRepository,
                                       PasswordEncoder passwordEncoder,
                                       @Value("${app.admin.email}") String adminEmail,
                                       @Value("${app.admin.password}") String adminPassword) {
        return args -> {
            if (userRepository.existsByEmail(adminEmail)) {
                return;
            }
            Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                    .orElseThrow(() -> new IllegalStateException("Roles missing - did Flyway run V1?"));

            userRepository.save(User.builder()
                    .fullName("Administrator")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(adminRole)
                    .enabled(true)
                    .build());
            log.info("Created default admin user {}", adminEmail);
        };
    }
}
