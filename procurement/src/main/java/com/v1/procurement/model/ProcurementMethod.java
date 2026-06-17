package com.v1.procurement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "procurement_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcurementMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "method_code", unique = true, nullable = false)
    private String methodCode;

    @Column(name = "method_name", nullable = false)
    private String methodName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "local_min_value")
    private BigDecimal localMinValue;

    @Column(name = "local_max_value")
    private BigDecimal localMaxValue;

    @Column(name = "foreign_min_value")
    private BigDecimal foreignMinValue;

    @Column(name = "foreign_max_value")
    private BigDecimal foreignMaxValue;

    @Column(name = "requires_newspaper_publication", nullable = false)
    private Boolean requiresNewspaperPublication;

    @Column(name = "requires_promise_lk_posting", nullable = false)
    private Boolean requiresPromiseLkPosting;

    @Column(name = "minimum_bid_period_days")
    private Integer minimumBidPeriodDays;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
