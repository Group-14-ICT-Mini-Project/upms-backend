package com.v1.auth.controller;

import com.v1.auth.dto.LoginRequest;
import com.v1.auth.dto.LoginResponse;
import com.v1.auth.dto.RefreshTokenRequest;
import com.v1.auth.model.User;
import com.v1.auth.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication endpoints for UPMS")
public class AuthenticationController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // In a real application, inject the UserService and UserRepository
    // This is a simplified demonstration

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate user with username/email and password. Returns JWT access token and refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "400", description = "Missing required fields")
            }
    )
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login attempt for user: {}", loginRequest.getUsername());

            // TODO: Implement actual authentication with UserRepository
            // For now, this is a placeholder that demonstrates the structure

            // In production:
            // 1. Find user by username or email
            // 2. Validate password using passwordEncoder.matches()
            // 3. Update lastLogin timestamp
            // 4. Generate tokens
            // 5. Create audit log entry

            if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Username and password are required");
            }

            // Placeholder response - replace with actual authentication logic
            LoginResponse response = LoginResponse.builder()
                    .accessToken("placeholder_access_token")
                    .refreshToken("placeholder_refresh_token")
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .userId(1L)
                    .username(loginRequest.getUsername())
                    .email(loginRequest.getUsername())
                    .roles(java.util.Set.of("USER"))
                    .lastLogin(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Refresh access token",
            description = "Generate new access token using valid refresh token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
            }
    )
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            if (request.getRefreshToken() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Refresh token is required");
            }

            // TODO: Implement actual token refresh logic
            // 1. Validate refresh token (not revoked, not expired)
            // 2. Extract user info from refresh token
            // 3. Generate new access token
            // 4. Return new access token

            LoginResponse response = LoginResponse.builder()
                    .accessToken("new_access_token")
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Token refresh error: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token refresh failed");
        }
    }

    @GetMapping("/verify")
    @Operation(
            summary = "Verify JWT token",
            description = "Verify if provided JWT token is valid",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token is valid"),
                    @ApiResponse(responseCode = "401", description = "Token is invalid or expired")
            }
    )
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Authorization header with Bearer token required");
            }

            String token = authHeader.substring(7);

            if (jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.ok("Token is valid");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token is invalid or expired");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token verification failed");
        }
    }

    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = "Logout user and revoke refresh token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logout successful")
            }
    )
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            // TODO: Implement logout logic
            // 1. Extract token and user info
            // 2. Revoke refresh tokens
            // 3. Clear any session data
            // 4. Create audit log entry

            return ResponseEntity.ok("Logout successful");

        } catch (Exception e) {
            log.error("Logout error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Logout failed");
        }
    }

    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Service health status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service is healthy")
            }
    )
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}
