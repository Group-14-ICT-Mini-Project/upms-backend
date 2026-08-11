package com.v1.procurement.controller;

import com.v1.procurement.dto.FacultyBudgetAllocationRequest;
import com.v1.procurement.dto.FacultyBudgetAllocationResponse;
import com.v1.procurement.service.FacultyBudgetAllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/budget-allocations")
@Tag(name = "Budget Allocations", description = "Finance Division faculty-wise procurement budget allocations")
@SecurityRequirement(name = "bearerAuth")
public class FacultyBudgetAllocationController {
    private final FacultyBudgetAllocationService budgetService;

    public FacultyBudgetAllocationController(FacultyBudgetAllocationService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    @Operation(summary = "List faculty budget allocations")
    public ResponseEntity<?> listAllocations(@RequestParam(required = false) Integer fiscalYear) {
        try {
            List<FacultyBudgetAllocationResponse> response = budgetService.listAllocations(fiscalYear);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to list faculty budget allocations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to list faculty budget allocations");
        }
    }

    @GetMapping("/{faculty}")
    @Operation(summary = "Get one faculty budget allocation")
    public ResponseEntity<?> getFacultyAllocation(
            @PathVariable String faculty,
            @RequestParam(required = false) Integer fiscalYear) {
        try {
            return ResponseEntity.ok(budgetService.getFacultyAllocation(faculty, fiscalYear));
        } catch (Exception e) {
            log.error("Failed to retrieve faculty budget allocation", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Faculty budget allocation not found");
        }
    }

    @PostMapping
    @Operation(summary = "Create or update a faculty budget allocation")
    public ResponseEntity<?> upsertAllocation(
            @Valid @RequestBody FacultyBudgetAllocationRequest request,
            Authentication authentication) {
        try {
            if ((request.getUpdatedBy() == null || request.getUpdatedBy().isBlank()) && authentication != null) {
                request.setUpdatedBy(authentication.getName());
            }
            FacultyBudgetAllocationResponse response = budgetService.upsertAllocation(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to save faculty budget allocation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to save faculty budget allocation: " + e.getMessage());
        }
    }
}
