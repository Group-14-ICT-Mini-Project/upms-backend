package com.v1.procurement.service;

import com.v1.procurement.dto.FacultyBudgetAllocationRequest;
import com.v1.procurement.dto.FacultyBudgetAllocationResponse;
import com.v1.procurement.model.FacultyBudgetAllocation;
import com.v1.procurement.model.Procurement;
import com.v1.procurement.model.ProcurementStatus;
import com.v1.procurement.repository.FacultyBudgetAllocationRepository;
import com.v1.procurement.repository.ProcurementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;

@Service
@Transactional
public class FacultyBudgetAllocationService {
    private final FacultyBudgetAllocationRepository budgetRepository;
    private final ProcurementRepository procurementRepository;

    public FacultyBudgetAllocationService(FacultyBudgetAllocationRepository budgetRepository,
                                          ProcurementRepository procurementRepository) {
        this.budgetRepository = budgetRepository;
        this.procurementRepository = procurementRepository;
    }

    @Transactional(readOnly = true)
    public List<FacultyBudgetAllocationResponse> listAllocations(Integer fiscalYear) {
        int year = fiscalYear != null ? fiscalYear : Year.now().getValue();
        return budgetRepository.findByFiscalYearOrderByFacultyAsc(year)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FacultyBudgetAllocationResponse getFacultyAllocation(String faculty, Integer fiscalYear) {
        int year = fiscalYear != null ? fiscalYear : Year.now().getValue();
        FacultyBudgetAllocation allocation = budgetRepository
                .findByFacultyIgnoreCaseAndFiscalYear(faculty, year)
                .orElseThrow(() -> new RuntimeException("Faculty budget allocation not found"));
        return mapToResponse(allocation);
    }

    public FacultyBudgetAllocationResponse upsertAllocation(FacultyBudgetAllocationRequest request) {
        FacultyBudgetAllocation allocation = budgetRepository
                .findByFacultyIgnoreCaseAndFiscalYear(request.getFaculty(), request.getFiscalYear())
                .orElseGet(FacultyBudgetAllocation::new);

        allocation.setFaculty(request.getFaculty().trim());
        allocation.setFiscalYear(request.getFiscalYear());
        allocation.setBudgetCode(request.getBudgetCode().trim());
        allocation.setAllocation(request.getAllocation());
        allocation.setUpdatedBy(request.getUpdatedBy());

        return mapToResponse(budgetRepository.save(allocation));
    }

    private FacultyBudgetAllocationResponse mapToResponse(FacultyBudgetAllocation allocation) {
        BudgetUsage usage = calculateUsage(allocation.getFaculty());
        BigDecimal available = allocation.getAllocation()
                .subtract(usage.committed())
                .subtract(usage.spent());

        return FacultyBudgetAllocationResponse.builder()
                .id(allocation.getId())
                .faculty(allocation.getFaculty())
                .fiscalYear(allocation.getFiscalYear())
                .budgetCode(allocation.getBudgetCode())
                .allocation(allocation.getAllocation())
                .committed(usage.committed())
                .spent(usage.spent())
                .available(available)
                .updatedBy(allocation.getUpdatedBy())
                .createdDate(allocation.getCreatedDate())
                .updatedDate(allocation.getUpdatedDate())
                .build();
    }

    private BudgetUsage calculateUsage(String faculty) {
        List<Procurement> procurements = procurementRepository.findAll();
        BigDecimal committed = BigDecimal.ZERO;
        BigDecimal spent = BigDecimal.ZERO;

        for (Procurement procurement : procurements) {
            if (!sameText(procurement.getFaculty(), faculty) || ProcurementStatus.REJECTED.equals(procurement.getStatus())) {
                continue;
            }

            BigDecimal value = procurement.getEstimatedValue() != null ? procurement.getEstimatedValue() : BigDecimal.ZERO;
            if (ProcurementStatus.COMPLETED.equals(procurement.getStatus())) {
                spent = spent.add(value);
            } else {
                committed = committed.add(value);
            }
        }

        return new BudgetUsage(committed, spent);
    }

    private boolean sameText(String left, String right) {
        if (left == null || right == null) return false;
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private record BudgetUsage(BigDecimal committed, BigDecimal spent) {
    }
}
