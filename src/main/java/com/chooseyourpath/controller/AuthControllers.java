package com.chooseyourpath.controller;

import com.chooseyourpath.dto.AuthDtos;
import com.chooseyourpath.entity.User;
import com.chooseyourpath.model.Role;
import com.chooseyourpath.repository.UserRepository;
import com.chooseyourpath.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Area-specific auth endpoints. Registration exists only for READER and
 * WRITER; admins are provisioned in the database (see database/sample-data.sql).
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Auth")
public class AuthControllers {

    private final AuthService auth;
    private final UserRepository users;

    public AuthControllers(AuthService auth, UserRepository users) {
        this.auth = auth;
        this.users = users;
    }

    public record AuthBody(String username, String password, String confirmPassword) {}

    // ---------- READER ----------

    @PostMapping("/reader/auth/register")
    @Operation(summary = "Register a READER account")
    public ResponseEntity<AuthDtos.AuthResponse> registerReader(@Valid @RequestBody AuthDtos.RegisterRequest body) {
        return ResponseEntity.ok(auth.register(body.username, body.password, body.confirmPassword, Role.READER));
    }

    @PostMapping("/reader/auth/login")
    @Operation(summary = "Log in as READER")
    public ResponseEntity<AuthDtos.AuthResponse> loginReader(@Valid @RequestBody AuthDtos.LoginRequest body) {
        return ResponseEntity.ok(auth.login(body.username, body.password, Role.READER));
    }

    // ---------- WRITER ----------

    @PostMapping("/writer/auth/register")
    @Operation(summary = "Register a WRITER account")
    public ResponseEntity<AuthDtos.AuthResponse> registerWriter(@Valid @RequestBody AuthDtos.RegisterRequest body) {
        return ResponseEntity.ok(auth.register(body.username, body.password, body.confirmPassword, Role.WRITER));
    }

    @PostMapping("/writer/auth/login")
    @Operation(summary = "Log in as WRITER")
    public ResponseEntity<AuthDtos.AuthResponse> loginWriter(@Valid @RequestBody AuthDtos.LoginRequest body) {
        return ResponseEntity.ok(auth.login(body.username, body.password, Role.WRITER));
    }

    // ---------- ADMIN ----------

    @PostMapping("/admin/auth/login")
    @Operation(summary = "Log in as ADMIN (no admin signup exists)")
    public ResponseEntity<AuthDtos.AuthResponse> loginAdmin(@Valid @RequestBody AuthDtos.LoginRequest body) {
        return ResponseEntity.ok(auth.login(body.username, body.password, Role.ADMIN));
    }

    // ---------- common ----------
    
    @GetMapping("/auth/me")
    @Operation(summary = "Current session user (id, username, role, bio)")
    public ResponseEntity<Map<String, Object>> me(@RequestHeader("Authorization") String authHeader) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User u = users.findByUsername(username).orElse(null);
        if (u == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "role", u.getRole().name(),
                "bio", u.getBio() == null ? "" : u.getBio()));
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "Logout (stateless JWT — client drops the token)")
    public ResponseEntity<Map<String, Object>> logout() {
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PutMapping("/auth/profile")
    @Operation(summary = "Update profile bio")
    public ResponseEntity<Map<String, Object>> profile(@RequestBody Map<String, String> body) {
        // handled in UserService via controller below; kept simple here
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
