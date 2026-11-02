package com.shri.loan_management_system.config;

import com.shri.loan_management_system.admin.Admin;
import com.shri.loan_management_system.admin.AdminRepository;
import com.shri.loan_management_system.user.Role;
import com.shri.loan_management_system.user.User;
import com.shri.loan_management_system.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.info("No initial admin configured; skipping admin seeding");
            return;
        }
        if (adminPassword.length() < 8) {
            throw new IllegalStateException("ADMIN_PASSWORD must be at least 8 characters");
        }

        String email = adminEmail.trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return;   // already seeded
        }

        User user = new User();
        user.setName("System Admin");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(adminPassword));
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        Admin admin = new Admin();
        admin.setUser(user);
        admin.setEmployeeId("ADM-001");
        admin.setDepartment("Administration");
        adminRepository.save(admin);

        log.info("Seeded initial admin account: {}", email);
    }
}