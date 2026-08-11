package com.v1.procurement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "faculty_budget_allocations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_faculty_budget_year", columnNames = {"faculty", "fiscal_year"})
}, indexes = {
        @Index(name = "idx_faculty_budget_faculty", columnList = "faculty"),
        @Index(name = "idx_faculty_budget_year", columnList = "fiscal_year")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyBudgetAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "faculty", nullable = false)
    private String faculty;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Column(name = "budget_code", nullable = false)
    private String budgetCode;

    @Column(name = "allocation", nullable = false)
    private BigDecimal allocation;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
