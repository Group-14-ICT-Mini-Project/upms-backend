package com.v1.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Signup response for access requests")
public class SignupResponse {
    private Long userId;
    private String username;
    private String email;
    private String approvalStatus;
    private String message;
}
