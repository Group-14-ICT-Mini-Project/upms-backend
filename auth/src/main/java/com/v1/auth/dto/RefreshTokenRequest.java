package com.v1.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Token refresh request")
public class RefreshTokenRequest {
    @Schema(description = "Refresh token", example = "refresh_token_value")
    private String refreshToken;
}
