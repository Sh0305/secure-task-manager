package com.securetask.taskmanager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securetask.taskmanager.exception.ResourceNotFoundException;
import com.securetask.taskmanager.model.AuditLog;
import com.securetask.taskmanager.model.User;
import com.securetask.taskmanager.repository.AuditLogRepository;
import com.securetask.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    // See all registered users
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Delete a user by id
   @DeleteMapping("/users/{id}")
   public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted");
    }

    // Promote or demote a user's role
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<String> changeRole(
            @PathVariable Long id,
            @RequestParam String role) {
        User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", id));
        try {
           user.setRole(User.Role.valueOf(role.toUpperCase()));
        }
        catch (IllegalArgumentException e) {
            throw new InvalidRequestException(
            "Role must be ADMIN or USER");
        }
        userRepository.save(user);
        return ResponseEntity.ok("Role updated to " + role.toUpperCase());
    }

    // See the full audit trail
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAll());
    }
}
