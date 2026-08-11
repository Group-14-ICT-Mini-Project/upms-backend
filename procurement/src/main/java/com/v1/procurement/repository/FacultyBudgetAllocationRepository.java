package com.v1.procurement.repository;

import com.v1.procurement.model.FacultyBudgetAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyBudgetAllocationRepository extends JpaRepository<FacultyBudgetAllocation, Long> {
    List<FacultyBudgetAllocation> findByFiscalYearOrderByFacultyAsc(Integer fiscalYear);

    Optional<FacultyBudgetAllocation> findByFacultyIgnoreCaseAndFiscalYear(String faculty, Integer fiscalYear);
}
