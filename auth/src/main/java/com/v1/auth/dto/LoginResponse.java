package com.v1.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Login response with JWT tokens")
public class LoginResponse {
    @Schema(description = "Access token (JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh token", example = "refresh_token_value")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Token expiration in seconds", example = "3600")
    private Long expiresIn;

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "User roles")
    private Set<String> roles;

    @Schema(description = "Last login time")
    private LocalDateTime lastLogin;
}
