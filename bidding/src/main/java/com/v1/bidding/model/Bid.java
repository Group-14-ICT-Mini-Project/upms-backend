package com.v1.bidding.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids", indexes = {
        @Index(name = "idx_procurement_id", columnList = "procurement_id"),
        @Index(name = "idx_bidder_id", columnList = "bidder_id"),
        @Index(name = "idx_bid_reference", columnList = "bid_reference"),
        @Index(name = "idx_submitted_date", columnList = "submitted_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "procurement_id", nullable = false)
    private Long procurementId;

    @Column(name = "bidder_id", nullable = false)
    private Long bidderId;

    @Column(name = "bid_reference", unique = true, nullable = false)
    private String bidReference;

    @Column(name = "submitted_date", nullable = false)
    private LocalDateTime submittedDate;

    @Column(name = "financial_bid", nullable = false)
    private BigDecimal financialBid;

    @Column(name = "delivery_schedule")
    private String deliverySchedule;

    @Column(name = "warranty_period")
    private String warrantyPeriod;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "bid_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BidStatus bidStatus;

    @Column(name = "is_compliant", nullable = false)
    private Boolean isCompliant;

    @Column(name = "compliance_notes", columnDefinition = "TEXT")
    private String complianceNotes;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (bidStatus == null) {
            bidStatus = BidStatus.SUBMITTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
