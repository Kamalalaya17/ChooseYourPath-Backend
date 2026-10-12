
package com.chooseyourpath.controller;

import com.chooseyourpath.dto.ResponseDtos;
import com.chooseyourpath.model.Role;
import com.chooseyourpath.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService admin;

    public AdminController(AdminService admin) {
        this.admin = admin;
    }

    private void requireAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Please log in as an administrator."
            );
        }

        // Adapt this check if your application stores the role
        // differently in its Spring Security authentication.
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ADMIN"));

        if (!isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Administrator access is required."
            );
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Get admin dashboard statistics")
    public ResponseEntity<ResponseDtos.StatsResponse> stats(
            Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(admin.stats());
    }

    @GetMapping("/users")
    @Operation(summary = "List users")
    public ResponseEntity<List<ResponseDtos.UserResponse>> users(
            @RequestParam(required = false) String role,
            Authentication authentication) {
        requireAdmin(authentication);

        Role parsedRole = null;
        if (role != null && !role.isBlank()) {
            parsedRole = Role.valueOf(role.toUpperCase());
        }

        return ResponseEntity.ok(admin.listUsers(parsedRole));
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Update a user's role")
    public ResponseEntity<Map<String, Boolean>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        requireAdmin(authentication);
        admin.setRole(id, body.getOrDefault("role", ""));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<Map<String, Boolean>> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {
        requireAdmin(authentication);
        admin.deleteUser(admin.userIdOf(authentication.getName()), id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/stories")
    @Operation(summary = "List stories for administration")
    public ResponseEntity<List<AdminService.AdminStory>> stories(
            @RequestParam(required = false) String status,
            Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(admin.listStories(status));
    }

    @GetMapping("/stories/{id}/review")
    @Operation(summary = "Get complete story content for admin review")
    public ResponseEntity<Map<String, Object>> reviewStory(
            @PathVariable Long id,
            Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(admin.reviewStory(id));
    }

    @PutMapping("/stories/{id}/approve")
    @Operation(summary = "Approve a story")
    public ResponseEntity<Map<String, Boolean>> approve(
            @PathVariable Long id,
            Authentication authentication) {
        requireAdmin(authentication);
        admin.approveStory(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PutMapping("/stories/{id}/reject")
    @Operation(summary = "Reject a story")
    public ResponseEntity<Map<String, Boolean>> reject(
            @PathVariable Long id,
            Authentication authentication) {
        requireAdmin(authentication);
        admin.rejectStory(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/stories/{id}")
    @Operation(summary = "Delete a story")
    public ResponseEntity<Map<String, Boolean>> deleteStory(
            @PathVariable Long id,
            Authentication authentication) {
        requireAdmin(authentication);
        admin.deleteStory(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/comments")
    @Operation(summary = "List comments")
    public ResponseEntity<List<ResponseDtos.CommentResponse>> comments(
            Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(admin.allComments());
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<Map<String, Boolean>> deleteComment(
            @PathVariable Long id,
            Authentication authentication) {
        requireAdmin(authentication);
        admin.deleteComment(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
