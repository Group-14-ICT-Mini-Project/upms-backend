package com.v1.auth.service.impl;

import com.v1.auth.dto.AdminUserResponse;
import com.v1.auth.dto.PendingUserResponse;
import com.v1.auth.dto.RoleResponse;
import com.v1.auth.dto.UpdateAdminUserRequest;
import com.v1.auth.dto.UserApprovalActionResponse;
import com.v1.auth.model.ApprovalStatus;
import com.v1.auth.model.AuditLog;
import com.v1.auth.model.Role;
import com.v1.auth.model.User;
import com.v1.auth.repository.AuditLogRepository;
import com.v1.auth.repository.RoleRepository;
import com.v1.auth.repository.UserRepository;
import com.v1.auth.service.AdminUserService;
import com.v1.auth.service.EmailNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toAdminUserResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingUserResponse> getPendingUsers() {
        return userRepository.findByApprovalStatusOrderByCreatedAtAsc(ApprovalStatus.PENDING)
                .stream()
                .map(this::toPendingUserResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        return roleRepository.findAll()
                .stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .build())
                .toList();
    }

    @Override
    public UserApprovalActionResponse approveUser(Long userId, String adminUsername) {
        User user = findPendingUser(userId);
        LocalDateTime now = LocalDateTime.now();

        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setIsActive(true);
        user.setApprovedAt(now);
        user.setApprovedBy(adminUsername);
        user.setRejectedAt(null);
        user.setRejectedBy(null);
        user.setRejectionReason(null);
        user.setUpdatedBy(adminUsername);
        User savedUser = userRepository.save(user);

        boolean emailSent = false;
        String emailWarning = null;
        try {
            emailNotificationService.sendAccessGrantedEmail(savedUser);
            emailSent = true;
        } catch (RuntimeException ex) {
            emailWarning = ex.getMessage();
            log.warn("User {} was approved, but approval email failed: {}", savedUser.getId(), ex.getMessage());
        }

        Map<String, Object> details = buildAuditDetails(savedUser, "User access approved");
        details.put("emailSent", emailSent);
        if (emailWarning != null) {
            details.put("emailWarning", emailWarning);
        }
        writeAuditLog(savedUser.getId(), "APPROVE_USER", String.valueOf(savedUser.getId()), "SUCCESS", details);

        return UserApprovalActionResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .approvalStatus(savedUser.getApprovalStatus().name())
                .message("User access approved.")
                .emailSent(emailSent)
                .emailWarning(emailWarning)
                .build();
    }

    @Override
    public UserApprovalActionResponse rejectUser(Long userId, String adminUsername, String reason) {
        User user = findPendingUser(userId);
        LocalDateTime now = LocalDateTime.now();
        String trimmedReason = reason == null || reason.isBlank() ? null : reason.trim();

        user.setApprovalStatus(ApprovalStatus.REJECTED);
        user.setIsActive(false);
        user.setRejectedAt(now);
        user.setRejectedBy(adminUsername);
        user.setRejectionReason(trimmedReason);
        user.setUpdatedBy(adminUsername);
        User savedUser = userRepository.save(user);

        Map<String, Object> details = buildAuditDetails(savedUser, "User access rejected");
        if (trimmedReason != null) {
            details.put("reason", trimmedReason);
        }
        writeAuditLog(savedUser.getId(), "REJECT_USER", String.valueOf(savedUser.getId()), "SUCCESS", details);

        return UserApprovalActionResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .approvalStatus(savedUser.getApprovalStatus().name())
                .message("User access rejected.")
                .emailSent(false)
                .build();
    }

    @Override
    public AdminUserResponse updateUser(Long userId, UpdateAdminUserRequest request, String adminUsername) {
        if (request == null) {
            throw new IllegalArgumentException("Update request is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        boolean updatingSelf = user.getUsername().equals(adminUsername);

        String username = trimToNull(request.getUsername());
        if (username != null && !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(username);
        }

        String email = trimToNull(request.getEmail());
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(email);
        }

        user.setFirstName(trimToNull(request.getFirstName()));
        user.setLastName(trimToNull(request.getLastName()));
        user.setFaculty(trimToNull(request.getFaculty()));
        user.setDepartment(trimToNull(request.getDepartment()));

        String password = trimToNull(request.getPassword());
        if (password != null) {
            if (password.length() < 8) {
                throw new IllegalArgumentException("Password must be at least 8 characters");
            }
            user.setPasswordHash(passwordEncoder.encode(password));
        }

        ApprovalStatus approvalStatus = resolveApprovalStatus(request.getApprovalStatus(), user.getApprovalStatus());
        Boolean active = request.getIsActive();
        if (updatingSelf && (approvalStatus != ApprovalStatus.APPROVED || Boolean.FALSE.equals(active))) {
            throw new IllegalArgumentException("You cannot disable or unapprove your own admin account");
        }

        user.setApprovalStatus(approvalStatus);
        if (active != null) {
            user.setIsActive(active);
        }

        if (approvalStatus == ApprovalStatus.APPROVED) {
            user.setApprovedAt(user.getApprovedAt() == null ? LocalDateTime.now() : user.getApprovedAt());
            user.setApprovedBy(user.getApprovedBy() == null ? adminUsername : user.getApprovedBy());
            user.setRejectedAt(null);
            user.setRejectedBy(null);
            user.setRejectionReason(null);
        } else if (approvalStatus == ApprovalStatus.REJECTED) {
            user.setIsActive(false);
            user.setRejectedAt(user.getRejectedAt() == null ? LocalDateTime.now() : user.getRejectedAt());
            user.setRejectedBy(user.getRejectedBy() == null ? adminUsername : user.getRejectedBy());
        }

        if (request.getRoles() != null) {
            Set<Role> roles = request.getRoles()
                    .stream()
                    .map(this::resolveRole)
                    .collect(Collectors.toSet());
            if (roles.isEmpty()) {
                throw new IllegalArgumentException("At least one role is required");
            }
            if (updatingSelf && roles.stream().noneMatch(role -> Role.RoleEnum.ADMIN.name().equals(role.getName()))) {
                throw new IllegalArgumentException("You cannot remove ADMIN from your own account");
            }
            user.setRoles(roles);
        }

        user.setUpdatedBy(adminUsername);
        User savedUser = userRepository.save(user);
        writeAuditLog(savedUser.getId(), "UPDATE_USER", String.valueOf(savedUser.getId()), "SUCCESS", buildAuditDetails(savedUser, "User account updated"));

        return toAdminUserResponse(savedUser);
    }

    @Override
    public void deleteUser(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getUsername().equals(adminUsername)) {
            throw new IllegalArgumentException("You cannot delete your own admin account");
        }

        Map<String, Object> details = buildAuditDetails(user, "User account deleted");
        writeAuditLog(user.getId(), "DELETE_USER", String.valueOf(user.getId()), "SUCCESS", details);
        userRepository.delete(user);
    }

    private User findPendingUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new IllegalArgumentException("Only pending users can be approved or rejected");
        }
        return user;
    }

    private PendingUserResponse toPendingUserResponse(User user) {
        return PendingUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .faculty(user.getFaculty())
                .department(user.getDepartment())
                .roles(extractRoleNames(user.getRoles()))
                .approvalStatus(user.getApprovalStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .faculty(user.getFaculty())
                .department(user.getDepartment())
                .isActive(user.getIsActive())
                .approvalStatus(user.getApprovalStatus() == null ? null : user.getApprovalStatus().name())
                .roles(extractRoleNames(user.getRoles()))
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private Role resolveRole(String roleName) {
        String normalized = trimToNull(roleName);
        if (normalized == null) {
            throw new IllegalArgumentException("Role name is required");
        }
        return roleRepository.findByName(normalized.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalized));
    }

    private ApprovalStatus resolveApprovalStatus(String requestedStatus, ApprovalStatus currentStatus) {
        String normalized = trimToNull(requestedStatus);
        if (normalized == null) {
            return currentStatus == null ? ApprovalStatus.PENDING : currentStatus;
        }
        try {
            return ApprovalStatus.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid approval status");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Set<String> extractRoleNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream().map(Role::getName).collect(Collectors.toSet());
    }

    private void writeAuditLog(Long userId, String action, String resourceId, String status, Map<String, Object> details) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .resourceType("USER")
                .resourceId(resourceId)
                .status(status)
                .details(details)
                .build();
        auditLogRepository.save(auditLog);
    }

    private Map<String, Object> buildAuditDetails(User user, String message) {
        Map<String, Object> details = new HashMap<>();
        details.put("username", user.getUsername());
        details.put("email", user.getEmail());
        details.put("message", message);
        return details;
    }
}
