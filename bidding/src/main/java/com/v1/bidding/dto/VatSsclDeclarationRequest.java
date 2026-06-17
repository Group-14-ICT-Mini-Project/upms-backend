package com.v1.bidding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "VAT/SSCL declaration")
public class VatSsclDeclarationRequest {

    @NotBlank(message = "Registration number is required")
    @Schema(description = "VAT/SSCL registration number", example = "123456789")
    private String registrationNumber;

    @NotBlank(message = "Registration type is required")
    @Schema(description = "Registration type (VAT or SSCL)", example = "VAT")
    private String registrationType;

    @Schema(description = "Whether declared", example = "true")
    private Boolean isDeclared;
}
