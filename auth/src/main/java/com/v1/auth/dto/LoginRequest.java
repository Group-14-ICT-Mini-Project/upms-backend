package com.v1.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Login request")
public class LoginRequest {
    @Schema(description = "Username or email", example = "user@example.com")
    private String username;

    @Schema(description = "Password", example = "password123")
    private String password;
}
