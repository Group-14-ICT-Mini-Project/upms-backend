package com.v1.bidding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bid bond details")
public class BidBondRequest {

    @NotBlank(message = "Bond reference is required")
    @Schema(description = "Bond reference number", example = "BD-2024-001")
    private String bondReference;

    @NotBlank(message = "Issuer bank is required")
    @Schema(description = "Bank issuing the bond", example = "Commercial Bank of Ceylon")
    private String issuerBank;

    @NotNull(message = "Bond amount is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Bond amount", example = "50000.00")
    private BigDecimal bondAmount;

    @NotNull(message = "Issued date is required")
    @Schema(description = "Bond issuance date")
    private LocalDate issuedDate;

    @NotNull(message = "Expiry date is required")
    @Schema(description = "Bond expiry date (must be at least 120 days from today)")
    private LocalDate expiryDate;
}
