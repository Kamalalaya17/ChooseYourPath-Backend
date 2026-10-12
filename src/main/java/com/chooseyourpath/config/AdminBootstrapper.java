package com.chooseyourpath.config;

import com.chooseyourpath.entity.User;
import com.chooseyourpath.model.Role;
import com.chooseyourpath.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrapper implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_BOOTSTRAP_USERNAME:}")
    private String adminUsername;

    @Value("${ADMIN_BOOTSTRAP_PASSWORD:}")
    private String adminPassword;

    public AdminBootstrapper(
            UserRepository users,
            PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // Do nothing unless both variables are provided.
        if (adminUsername == null || adminUsername.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            return;
        }

        User admin;

        // Find existing admin account.
        if (users.existsByUsername(adminUsername)) {

            admin = users.findByUsername(adminUsername)
                    .orElseThrow(() -> new IllegalStateException(
                            "Admin username exists but could not be loaded."
                    ));

            // Only reset the password for an ADMIN account.
            if (admin.getRole() != Role.ADMIN) {
                System.out.println(
                        "Admin bootstrap stopped: existing username is not an ADMIN."
                );
                return;
            }

            admin.setPassword(passwordEncoder.encode(adminPassword));
            users.save(admin);

            System.out.println(
                    "Existing admin password reset successfully."
            );

            return;
        }

        // Create admin if it does not exist.
        admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);

        users.save(admin);

        System.out.println(
                "Admin account created successfully."
        );
    }
}