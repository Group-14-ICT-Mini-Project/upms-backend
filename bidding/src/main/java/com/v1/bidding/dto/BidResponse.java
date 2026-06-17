package com.v1.bidding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bid response with all details")
public class BidResponse {

    @Schema(description = "Bid ID")
    private Long id;

    @Schema(description = "Procurement ID")
    private Long procurementId;

    @Schema(description = "Bidder ID")
    private Long bidderId;

    @Schema(description = "Bid reference number", example = "BID-2024-001")
    private String bidReference;

    @Schema(description = "Submitted date")
    private LocalDateTime submittedDate;

    @Schema(description = "Financial bid amount")
    private BigDecimal financialBid;

    @Schema(description = "Delivery schedule")
    private String deliverySchedule;

    @Schema(description = "Warranty period")
    private String warrantyPeriod;

    @Schema(description = "Payment terms")
    private String paymentTerms;

    @Schema(description = "Current bid status")
    private String bidStatus;

    @Schema(description = "Whether bid is compliant with requirements")
    private Boolean isCompliant;

    @Schema(description = "Compliance notes")
    private String complianceNotes;

    @Schema(description = "Creation date")
    private LocalDateTime createdDate;

    @Schema(description = "Last update date")
    private LocalDateTime updatedDate;
}
