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
@Schema(description = "Procurement response")
public class ProcurementResponse {

    @Schema(description = "Procurement ID")
    private Long id;

    @Schema(description = "Reference number")
    private String referenceNumber;

    @Schema(description = "Procurement title")
    private String title;

    @Schema(description = "Procurement description")
    private String description;

    @Schema(description = "Estimated value")
    private BigDecimal estimatedValue;

    @Schema(description = "Procurement method name")
    private String procurementMethodName;

    @Schema(description = "Procurement category name")
    private String categoryName;

    @Schema(description = "Current status")
    private String status;

    @Schema(description = "Approval level")
    private String approvalLevel;

    @Schema(description = "Opening date")
    private LocalDateTime openingDate;

    @Schema(description = "Closing date")
    private LocalDateTime closingDate;

    @Schema(description = "Document fee")
    private BigDecimal documentFee;

    @Schema(description = "Bid bond requirement")
    private Boolean requiresBidBond;

    @Schema(description = "Bid bond percentage")
    private BigDecimal bidBondPercentage;

    @Schema(description = "Created by user ID")
    private Long createdByUserId;

    @Schema(description = "Created date")
    private LocalDateTime createdDate;

    @Schema(description = "Updated date")
    private LocalDateTime updatedDate;

    @Schema(description = "Is active")
    private Boolean isActive;

    @Schema(description = "Published date")
    private LocalDateTime publishedDate;

    @Schema(description = "Faculty raising the requisition")
    private String faculty;

    @Schema(description = "Department raising the requisition")
    private String department;

    @Schema(description = "Requisition type: Consumables or Capital Goods")
    private String requisitionType;

    @Schema(description = "Current stock balance at time of requisition")
    private Integer currentStockBalance;

    @Schema(description = "Source of funding for this procurement")
    private String fundingSource;

    @Schema(description = "Budget code assigned by the Bursar")
    private String budgetCode;

    @Schema(description = "Supplier name, set after Purchase Order is raised")
    private String supplierName;

    @Schema(description = "Purchase Order number, set by the Supplies Division (SDC)")
    private String poNumber;

    @Schema(description = "GRN number, set by the Storekeeper")
    private String grnNumber;

    @Schema(description = "Invoice number, set by the Finance Division")
    private String invoiceNumber;

    @Schema(description = "Invoice amount, set by the Finance Division")
    private BigDecimal invoiceAmount;
}
