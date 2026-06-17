package com.v1.bidding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to submit a bid")
public class SubmitBidRequest {

    @NotNull(message = "Procurement ID is required")
    @Schema(description = "Procurement ID", example = "1")
    private Long procurementId;

    @NotNull(message = "Financial bid is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Bid amount in LKR", example = "450000.00")
    private BigDecimal financialBid;

    @Schema(description = "Delivery schedule", example = "15 days from PO")
    private String deliverySchedule;

    @Schema(description = "Warranty period", example = "1 year")
    private String warrantyPeriod;

    @Schema(description = "Payment terms", example = "Net 30")
    private String paymentTerms;

    @NotNull(message = "Bid bond details required")
    @Schema(description = "Bid bond details")
    private BidBondRequest bidBond;

    @NotNull(message = "Document fee payment required")
    @Schema(description = "Document fee payment reference")
    private String documentFeeReference;

    @NotNull(message = "VAT/SSCL registration required")
    @Schema(description = "VAT/SSCL registration")
    private VatSsclDeclarationRequest vatSsclDeclaration;
}
