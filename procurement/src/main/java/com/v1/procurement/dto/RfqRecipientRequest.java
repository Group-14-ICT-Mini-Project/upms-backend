package com.v1.procurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to add RFQ recipient")
public class RfqRecipientRequest {

    @NotBlank(message = "Supplier email is required")
    @Email(message = "Valid email is required")
    @Schema(description = "Supplier email address", example = "supplier@company.com")
    private String supplierEmail;

    @NotBlank(message = "Supplier name is required")
    @Schema(description = "Supplier name", example = "ABC Supplies Ltd")
    private String supplierName;

    @Schema(description = "Supplier ID", example = "SUP-001")
    private String supplierId;
}
