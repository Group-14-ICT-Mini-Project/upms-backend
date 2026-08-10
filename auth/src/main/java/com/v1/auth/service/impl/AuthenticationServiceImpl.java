package com.v1.auth.service.impl;

import com.v1.auth.dto.LoginRequest;
import com.v1.auth.dto.LoginResponse;
import com.v1.auth.dto.MicrosoftLoginRequest;
import com.v1.auth.dto.RefreshTokenRequest;
import com.v1.auth.dto.SignupRequest;
import com.v1.auth.dto.SignupResponse;
import com.v1.auth.model.AuditLog;
import com.v1.auth.model.ApprovalStatus;
import com.v1.auth.model.RefreshToken;
import com.v1.auth.model.Role;
import com.v1.auth.model.User;
import com.v1.auth.repository.AuditLogRepository;
import com.v1.auth.repository.RoleRepository;
import com.v1.auth.repository.RefreshTokenRepository;
import com.v1.auth.repository.UserRepository;
import com.v1.auth.security.JwtTokenProvider;
import com.v1.auth.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${app.microsoft.issuer-uri}")
    private String microsoftIssuerUri;

    @Value("${app.microsoft.tenant-id}")
    private String microsoftTenantId;

    @Value("${app.microsoft.client-id}")
    private String microsoftClientId;

    @Value("${app.microsoft.allowed-audience}")
    private String microsoftAllowedAudience;

    @Value("${app.microsoft.allowed-domain:}")
    private String microsoftAllowedDomain;

    private JwtDecoder microsoftJwtDecoder;

    public LoginResponse login(LoginRequest request) {
        validateLoginRequest(request);

        String loginValue = request.getUsername().trim();
        User user = userRepository.findByUsername(loginValue)
                .or(() -> userRepository.findByEmail(loginValue))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        approveTestingAdminIfNeeded(user);
        enforceApprovedUser(user);

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), extractRoleNames(user.getRoles()));
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        persistRefreshToken(user.getId(), refreshToken);
        writeAuditLog(user.getId(), "LOGIN", "AUTHENTICATION", String.valueOf(user.getId()), "SUCCESS", buildAuditDetails(user, "Login successful"));

        return buildLoginResponse(user, accessToken, refreshToken);
    }

    @Override
    public SignupResponse signup(SignupRequest request) {
        validateSignupRequest(request);

        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role role = roleRepository.findByName(resolveSignupRole(request.getRole()))
                .orElseThrow(() -> new IllegalArgumentException("Requested role not found"));
        boolean isTestingAdminSignup = Role.RoleEnum.ADMIN.name().equals(role.getName());
        LocalDateTime approvedAt = isTestingAdminSignup ? LocalDateTime.now() : null;

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim())
                .firstName(trimToNull(request.getFirstName()))
                .lastName(trimToNull(request.getLastName()))
                .faculty(resolveFaculty(request, role.getName()))
                .department(resolveDepartment(request, role.getName()))
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(isTestingAdminSignup)
                .approvalStatus(isTestingAdminSignup ? ApprovalStatus.APPROVED : ApprovalStatus.PENDING)
                .approvedAt(approvedAt)
                .approvedBy(isTestingAdminSignup ? "SELF_REGISTRATION_TEST_BYPASS" : null)
                .createdBy("SELF_REGISTRATION")
                .updatedBy("SELF_REGISTRATION")
                .build();
        user.addRole(role);

        User savedUser = userRepository.save(user);

        String auditMessage = isTestingAdminSignup
                ? "Testing admin account registered with approval bypass"
                : "Access request submitted for admin approval";
        writeAuditLog(savedUser.getId(), "SIGNUP_REQUEST", "AUTHENTICATION", String.valueOf(savedUser.getId()), "SUCCESS", buildAuditDetails(savedUser, auditMessage));

        return SignupResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .approvalStatus(savedUser.getApprovalStatus().name())
                .message(isTestingAdminSignup
                        ? "Testing admin account created. You can sign in now."
                        : "Your access request has been submitted and is waiting for administrator approval.")
                .build();
    }

    @Override
    public LoginResponse microsoftLogin(MicrosoftLoginRequest request) {
        if (request == null || request.getAccessToken() == null || request.getAccessToken().isBlank()) {
            throw new IllegalArgumentException("Microsoft access token is required");
        }

        Jwt microsoftToken = decodeMicrosoftToken(request.getAccessToken().trim());
        validateMicrosoftToken(microsoftToken);

        String tenantId = trimToNull(microsoftToken.getClaimAsString("tid"));
        String objectId = trimToNull(microsoftToken.getClaimAsString("oid"));
        String email = resolveMicrosoftEmail(microsoftToken);

        if (tenantId == null || objectId == null) {
            throw new RuntimeException("Microsoft token is missing required identity claims");
        }

        String azureId = tenantId + ":" + objectId;
        User user = userRepository.findByAzureId(azureId)
                .or(() -> userRepository.findByEmailIgnoreCase(email))
                .orElseThrow(() -> new SecurityException("UPMS access has not been approved for this Microsoft account"));

        approveTestingAdminIfNeeded(user);
        enforceApprovedUser(user);

        if (user.getAzureId() == null || user.getAzureId().isBlank()) {
            user.setAzureId(azureId);
        }

        user.setLastLogin(LocalDateTime.now());
        user.setUpdatedBy("MICROSOFT_LOGIN");
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), extractRoleNames(user.getRoles()));
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        persistRefreshToken(user.getId(), refreshToken);
        writeAuditLog(user.getId(), "MICROSOFT_LOGIN", "AUTHENTICATION", String.valueOf(user.getId()), "SUCCESS", buildAuditDetails(user, "Microsoft login successful"));

        return buildLoginResponse(user, accessToken, refreshToken);
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        String rawRefreshToken = request.getRefreshToken().trim();
        if (!jwtTokenProvider.validateToken(rawRefreshToken) || !jwtTokenProvider.isRefreshToken(rawRefreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
                .orElseThrow(() -> new RuntimeException("Refresh token is not recognized"));

        if (Boolean.TRUE.equals(storedToken.getIsRevoked()) || !storedToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            storedToken.setIsRevoked(true);
            storedToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(storedToken);
            throw new RuntimeException("Refresh token is revoked or expired");
        }

        Long userId = jwtTokenProvider.getUserIdFromJWT(rawRefreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        approveTestingAdminIfNeeded(user);
        enforceApprovedUser(user);

        storedToken.setIsRevoked(true);
        storedToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), extractRoleNames(user.getRoles()));
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        persistRefreshToken(user.getId(), newRefreshToken);
        writeAuditLog(user.getId(), "REFRESH_TOKEN", "AUTHENTICATION", String.valueOf(user.getId()), "SUCCESS", buildAuditDetails(user, "Token refreshed"));

        return buildLoginResponse(user, newAccessToken, newRefreshToken);
    }

    @Transactional(readOnly = true)
    public void verifyToken(String authHeader) {
        String token = extractBearerToken(authHeader);

        if (jwtTokenProvider.isRefreshToken(token)) {
            throw new RuntimeException("Refresh token cannot be used for verification");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("Token is invalid or expired");
        }

        Long userId = jwtTokenProvider.getUserIdFromJWT(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        approveTestingAdminIfNeeded(user);
        enforceApprovedUser(user);
    }

    public void logout(String authHeader) {
        String token = extractBearerToken(authHeader);

        if (jwtTokenProvider.isRefreshToken(token)) {
            throw new RuntimeException("Access token is required for logout");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("Token is invalid or expired");
        }

        Long userId = jwtTokenProvider.getUserIdFromJWT(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndIsRevokedFalse(userId);
        activeTokens.forEach(refreshToken -> {
            refreshToken.setIsRevoked(true);
            refreshToken.setRevokedAt(LocalDateTime.now());
        });
        refreshTokenRepository.saveAll(activeTokens);

        writeAuditLog(user.getId(), "LOGOUT", "AUTHENTICATION", String.valueOf(user.getId()), "SUCCESS", buildAuditDetails(user, "Logout successful"));
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getUsername().isBlank() || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Username and password are required");
        }
    }

    private void validateSignupRequest(SignupRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Signup request is required");
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        String role = resolveSignupRole(request.getRole());
        if (requiresFaculty(role) && trimToNull(request.getFaculty()) == null) {
            throw new IllegalArgumentException("Faculty is required for this role");
        }

        if (Role.RoleEnum.HOD.name().equals(role) && trimToNull(request.getDepartment()) == null) {
            throw new IllegalArgumentException("Department is required for HOD users");
        }
    }

    private void enforceApprovedUser(User user) {
        if (user.getApprovalStatus() == ApprovalStatus.PENDING) {
            throw new RuntimeException("Your access request is still waiting for administrator approval.");
        }

        if (user.getApprovalStatus() == ApprovalStatus.REJECTED) {
            throw new RuntimeException("Your access request was rejected. Please contact the system administrator.");
        }

        if (!Boolean.TRUE.equals(user.getIsActive()) || user.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new RuntimeException("User account is inactive");
        }
    }

    private void approveTestingAdminIfNeeded(User user) {
        if (!isAdminUser(user)) {
            return;
        }

        if (Boolean.TRUE.equals(user.getIsActive()) && user.getApprovalStatus() == ApprovalStatus.APPROVED) {
            return;
        }

        user.setIsActive(true);
        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setApprovedAt(LocalDateTime.now());
        user.setApprovedBy("ADMIN_TESTING_LOGIN_BYPASS");
        user.setRejectedAt(null);
        user.setRejectedBy(null);
        user.setRejectionReason(null);
        user.setUpdatedBy("ADMIN_TESTING_LOGIN_BYPASS");
        userRepository.save(user);
        writeAuditLog(user.getId(), "ADMIN_TESTING_APPROVAL_BYPASS", "AUTHENTICATION", String.valueOf(user.getId()), "SUCCESS", buildAuditDetails(user, "Admin testing account auto-approved during login"));
    }

    private boolean isAdminUser(User user) {
        return user.getRoles() != null
                && user.getRoles().stream().anyMatch(role -> Role.RoleEnum.ADMIN.name().equals(role.getName()));
    }

    private Jwt decodeMicrosoftToken(String accessToken) {
        try {
            if (microsoftJwtDecoder == null) {
                microsoftJwtDecoder = NimbusJwtDecoder.withIssuerLocation(microsoftIssuerUri).build();
            }

            return microsoftJwtDecoder.decode(accessToken);
        } catch (JwtException e) {
            throw new RuntimeException("Microsoft token is invalid or expired", e);
        }
    }

    private void validateMicrosoftToken(Jwt token) {
        String issuer = token.getIssuer() == null ? null : token.getIssuer().toString();
        if (!microsoftIssuerUri.equals(issuer)) {
            throw new RuntimeException("Microsoft token issuer is not trusted");
        }

        String tenantId = token.getClaimAsString("tid");
        if (!microsoftTenantId.equals(tenantId)) {
            throw new RuntimeException("Microsoft token tenant is not allowed");
        }

        List<String> audiences = token.getAudience();
        if (!audiences.contains(microsoftAllowedAudience) && !audiences.contains(microsoftClientId)) {
            throw new RuntimeException("Microsoft token audience is not allowed");
        }

        String email = resolveMicrosoftEmail(token);
        String allowedDomain = trimToNull(microsoftAllowedDomain);
        if (allowedDomain != null && !email.toLowerCase().endsWith("@" + allowedDomain.toLowerCase())) {
            throw new SecurityException("Only approved university Microsoft accounts can sign in");
        }
    }

    private String resolveMicrosoftEmail(Jwt token) {
        String email = trimToNull(token.getClaimAsString("preferred_username"));

        if (email == null) {
            email = trimToNull(token.getClaimAsString("email"));
        }

        if (email == null) {
            throw new RuntimeException("Microsoft token does not include an email address");
        }

        return email.toLowerCase();
    }

    private String resolveSignupRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return Role.RoleEnum.BIDDER.name();
        }

        return requestedRole.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean requiresFaculty(String role) {
        return Role.RoleEnum.HOD.name().equals(role)
                || Role.RoleEnum.FACULTY_BURSAR.name().equals(role)
                || Role.RoleEnum.FACULTY_DEAN.name().equals(role);
    }

    private String resolveFaculty(SignupRequest request, String role) {
        return requiresFaculty(role) ? trimToNull(request.getFaculty()) : null;
    }

    private String resolveDepartment(SignupRequest request, String role) {
        return Role.RoleEnum.HOD.name().equals(role) ? trimToNull(request.getDepartment()) : null;
    }

    private LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(Math.max(1L, jwtTokenProvider.getAccessTokenExpirationInMs() / 1000L))
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .faculty(user.getFaculty())
                .department(user.getDepartment())
                .roles(extractRoleNames(user.getRoles()))
                .lastLogin(user.getLastLogin())
                .build();
    }

    private Set<String> extractRoleNames(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return new HashSet<>();
        }

        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    private void persistRefreshToken(Long userId, String rawRefreshToken) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(hashToken(rawRefreshToken))
            .expiresAt(LocalDateTime.now().plusNanos(jwtTokenProvider.getRefreshTokenExpirationInMs() * 1_000_000L))
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private void writeAuditLog(Long userId, String action, String resourceType, String resourceId, String status, Map<String, Object> details) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .resourceType(resourceType)
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

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token required");
        }

        return authHeader.substring(7).trim();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to hash token", e);
        }
    }
}
