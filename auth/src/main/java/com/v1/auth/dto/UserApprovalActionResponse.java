package com.v1.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApprovalActionResponse {
    private Long userId;
    private String username;
    private String approvalStatus;
    private String message;
    private boolean emailSent;
    private String emailWarning;
}
