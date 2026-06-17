package com.v1.procurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new procurement")
public class CreateProcurementRequest {

    @NotBlank(message = "Title is required")
    @Schema(description = "Procurement title", example = "Purchase of Office Supplies")
    private String title;

    @Schema(description = "Detailed description", example = "Procurement for office supplies...")
    private String description;

    @NotNull(message = "Estimated value is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Estimated value in LKR", example = "500000.00")
    private BigDecimal estimatedValue;

    @NotNull(message = "Procurement method ID is required")
    @Schema(description = "Procurement method ID", example = "1")
    private Long procurementMethodId;

    @NotNull(message = "Procurement category ID is required")
    @Schema(description = "Category ID", example = "1")
    private Long procurementCategoryId;

    @NotNull(message = "Opening date is required")
    @Schema(description = "Bid opening date and time")
    private LocalDateTime openingDate;

    @NotNull(message = "Closing date is required")
    @Schema(description = "Bid closing date and time")
    private LocalDateTime closingDate;

    @NotNull(message = "Document fee is required")
    @DecimalMin(value = "0.0")
    @Schema(description = "Document fee amount", example = "8000.00")
    private BigDecimal documentFee;

    @NotNull(message = "Bid bond requirement must be specified")
    @Schema(description = "Whether bid bond is required", example = "true")
    private Boolean requiresBidBond;

    @Schema(description = "Bid bond as percentage of bid value", example = "5")
    private BigDecimal bidBondPercentage;
}
