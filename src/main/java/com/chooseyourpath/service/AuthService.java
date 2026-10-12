package com.chooseyourpath.service;

import com.chooseyourpath.dto.AuthDtos;
import com.chooseyourpath.entity.User;
import com.chooseyourpath.model.Role;
import com.chooseyourpath.repository.UserRepository;
import com.chooseyourpath.security.JwtService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthDtos.AuthResponse register(String username, String password, String confirmPassword, Role role) {
        if (role == Role.ADMIN) {
            throw new SecurityException("Admin accounts cannot be self-registered.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("That username is already taken.");
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(encoder.encode(password));
        u.setRole(role);
        users.save(u);
        return new AuthDtos.AuthResponse(jwt.generateToken(username, role.name()), username, role.name());
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(String username, String password, Role area) {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));
        if (!encoder.matches(password, u.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        if (area != null && u.getRole() != area) {
            throw new IllegalStateException(
                    "This is a " + u.getRole().name().toLowerCase() + " account. Use the "
                            + u.getRole().name().toLowerCase() + " portal.");
        }
        return new AuthDtos.AuthResponse(jwt.generateToken(u.getUsername(), u.getRole().name()),
                u.getUsername(), u.getRole().name());
    }

    @CacheEvict(value = "stories", allEntries = true)
    public void evictCaches() {
        // hook used after bulk changes
    }
}
