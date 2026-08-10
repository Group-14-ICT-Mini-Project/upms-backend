package com.v1.auth.controller;

import com.v1.auth.dto.RejectUserRequest;
import com.v1.auth.dto.UpdateAdminUserRequest;
import com.v1.auth.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin Users", description = "Admin user access approval endpoints")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "List all users")
    public ResponseEntity<?> allUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @GetMapping("/pending")
    @Operation(summary = "List pending access requests")
    public ResponseEntity<?> pendingUsers() {
        return ResponseEntity.ok(adminUserService.getPendingUsers());
    }

    @GetMapping("/roles")
    @Operation(summary = "List available roles")
    public ResponseEntity<?> roles() {
        return ResponseEntity.ok(adminUserService.getRoles());
    }

    @PostMapping("/{userId}/approve")
    @Operation(summary = "Approve a pending user")
    public ResponseEntity<?> approveUser(@PathVariable Long userId, Authentication authentication) {
        try {
            return ResponseEntity.ok(adminUserService.approveUser(userId, authentication.getName()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            log.warn("User approval failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/reject")
    @Operation(summary = "Reject a pending user")
    public ResponseEntity<?> rejectUser(
            @PathVariable Long userId,
            @RequestBody(required = false) RejectUserRequest request,
            Authentication authentication
    ) {
        try {
            String reason = request == null ? null : request.getReason();
            return ResponseEntity.ok(adminUserService.rejectUser(userId, authentication.getName(), reason));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            log.warn("User rejection failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user credentials, roles, and access state")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateAdminUserRequest request,
            Authentication authentication
    ) {
        try {
            return ResponseEntity.ok(adminUserService.updateUser(userId, request, authentication.getName()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            log.warn("User update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete a user account")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, Authentication authentication) {
        try {
            adminUserService.deleteUser(userId, authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            log.warn("User deletion failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
