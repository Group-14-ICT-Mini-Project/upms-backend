package com.v1.procurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Faculty-wise procurement budget allocation request")
public class FacultyBudgetAllocationRequest {
    @NotBlank
    @Schema(description = "Faculty receiving the procurement budget")
    private String faculty;

    @NotNull
    @Schema(description = "Fiscal year")
    private Integer fiscalYear;

    @NotBlank
    @Schema(description = "Finance budget code")
    private String budgetCode;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Schema(description = "Allocated procurement budget")
    private BigDecimal allocation;

    @Schema(description = "Finance user name or identifier")
    private String updatedBy;
}
