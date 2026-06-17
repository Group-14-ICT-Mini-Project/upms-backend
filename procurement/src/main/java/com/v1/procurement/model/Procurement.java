package com.v1.procurement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "procurements", indexes = {
        @Index(name = "idx_reference_number", columnList = "reference_number"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_by", columnList = "created_by_user_id"),
        @Index(name = "idx_created_date", columnList = "created_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Procurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", unique = true, nullable = false)
    private String referenceNumber;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "estimated_value", nullable = false)
    private BigDecimal estimatedValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "procurement_method_id", nullable = false)
    private ProcurementMethod procurementMethod;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "procurement_category_id", nullable = false)
    private ProcurementCategory procurementCategory;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProcurementStatus status;

    @Column(name = "approval_level")
    private String approvalLevel;

    @Column(name = "opening_date", nullable = false)
    private LocalDateTime openingDate;

    @Column(name = "closing_date", nullable = false)
    private LocalDateTime closingDate;

    @Column(name = "document_fee", nullable = false)
    private BigDecimal documentFee;

    @Column(name = "requires_bid_bond", nullable = false)
    private Boolean requiresBidBond;

    @Column(name = "bid_bond_percentage")
    private BigDecimal bidBondPercentage;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        isActive = true;
        if (status == null) {
            status = ProcurementStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
