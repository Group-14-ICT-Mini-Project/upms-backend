package com.v1.auth.controller;

import com.v1.auth.dto.LoginRequest;
import com.v1.auth.dto.LoginResponse;
import com.v1.auth.dto.RefreshTokenRequest;
import com.v1.auth.dto.SignupRequest;
import com.v1.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication endpoints for UPMS")
public class AuthenticationController {

        @Autowired
        private AuthenticationService authenticationService;

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
                        return ResponseEntity.ok(authenticationService.login(loginRequest));
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
                } catch (RuntimeException e) {
                        log.warn("Login failed: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
                }
        }

        @PostMapping("/signup")
        @Operation(
                        summary = "User signup",
                        description = "Create a new user account, assign a default role, and return JWT tokens.",
                        responses = {
                                        @ApiResponse(responseCode = "200", description = "Signup successful",
                                                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
                                        @ApiResponse(responseCode = "400", description = "Invalid signup data")
                        }
        )
        public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
                try {
                        return ResponseEntity.ok(authenticationService.signup(signupRequest));
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
                } catch (RuntimeException e) {
                        log.warn("Signup failed: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
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
                        return ResponseEntity.ok(authenticationService.refreshToken(request));
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
                } catch (RuntimeException e) {
                        log.warn("Token refresh failed: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
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
                        authenticationService.verifyToken(authHeader);
                        return ResponseEntity.ok("Token is valid");
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
                } catch (RuntimeException e) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
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
                        authenticationService.logout(authHeader);
                        return ResponseEntity.ok("Logout successful");
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
                } catch (RuntimeException e) {
                        log.warn("Logout failed: {}", e.getMessage());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
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
