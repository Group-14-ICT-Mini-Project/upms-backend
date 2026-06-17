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
@Schema(description = "Request to update procurement")
public class UpdateProcurementRequest {

    @Schema(description = "Procurement title")
    private String title;

    @Schema(description = "Procurement description")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Estimated value")
    private BigDecimal estimatedValue;

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
}
