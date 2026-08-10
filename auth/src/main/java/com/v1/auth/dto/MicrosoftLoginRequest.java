package com.v1.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MicrosoftLoginRequest {
    @NotBlank(message = "Microsoft access token is required")
    private String accessToken;
}
