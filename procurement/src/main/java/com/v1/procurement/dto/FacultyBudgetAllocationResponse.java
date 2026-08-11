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
@Schema(description = "Faculty-wise procurement budget allocation with current usage")
public class FacultyBudgetAllocationResponse {
    private Long id;
    private String faculty;
    private Integer fiscalYear;
    private String budgetCode;
    private BigDecimal allocation;
    private BigDecimal committed;
    private BigDecimal spent;
    private BigDecimal available;
    private String updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
