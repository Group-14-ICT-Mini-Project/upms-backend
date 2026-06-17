package com.v1.procurement.dto;

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
@Schema(description = "Procurement response with all details")
public class ProcurementResponse {

    @Schema(description = "Procurement ID")
    private Long id;

    @Schema(description = "Reference number", example = "PROC-2024-001")
    private String referenceNumber;

    @Schema(description = "Procurement title")
    private String title;

    @Schema(description = "Procurement description")
    private String description;

    @Schema(description = "Estimated value")
    private BigDecimal estimatedValue;

    @Schema(description = "Method name")
    private String procurementMethodName;

    @Schema(description = "Category name")
    private String categoryName;

    @Schema(description = "Current status")
    private String status;

    @Schema(description = "Approval level")
    private String approvalLevel;

    @Schema(description = "Bid opening date")
    private LocalDateTime openingDate;

    @Schema(description = "Bid closing date")
    private LocalDateTime closingDate;

    @Schema(description = "Document fee")
    private BigDecimal documentFee;

    @Schema(description = "Whether bid bond is required")
    private Boolean requiresBidBond;

    @Schema(description = "Bid bond percentage")
    private BigDecimal bidBondPercentage;

    @Schema(description = "Creator user ID")
    private Long createdByUserId;

    @Schema(description = "Creation date")
    private LocalDateTime createdDate;

    @Schema(description = "Last update date")
    private LocalDateTime updatedDate;

    @Schema(description = "Whether active")
    private Boolean isActive;

    @Schema(description = "Publication date")
    private LocalDateTime publishedDate;
}
