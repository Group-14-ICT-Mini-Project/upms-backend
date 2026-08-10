package com.v1.auth.service.impl;

import com.v1.auth.dto.PendingUserResponse;
import com.v1.auth.dto.UserApprovalActionResponse;
import com.v1.auth.model.ApprovalStatus;
import com.v1.auth.model.AuditLog;
import com.v1.auth.model.Role;
import com.v1.auth.model.User;
import com.v1.auth.repository.AuditLogRepository;
import com.v1.auth.repository.UserRepository;
import com.v1.auth.service.AdminUserService;
import com.v1.auth.service.EmailNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private EmailNotificationService emailNotificationService;

    @Override
    @Transactional(readOnly = true)
    public List<PendingUserResponse> getPendingUsers() {
        return userRepository.findByApprovalStatusOrderByCreatedAtAsc(ApprovalStatus.PENDING)
                .stream()
                .map(this::toPendingUserResponse)
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
