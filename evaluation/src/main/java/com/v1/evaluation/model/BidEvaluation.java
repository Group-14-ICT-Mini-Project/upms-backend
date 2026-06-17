package com.v1.evaluation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bid_evaluations", indexes = {
        @Index(name = "idx_procurement_id", columnList = "procurement_id"),
        @Index(name = "idx_bid_id", columnList = "bid_id"),
        @Index(name = "idx_evaluator_id", columnList = "evaluator_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "procurement_id", nullable = false)
    private Long procurementId;

    @Column(name = "bid_id", nullable = false)
    private Long bidId;

    @Column(name = "bidder_id", nullable = false)
    private Long bidderId;

    @Column(name = "evaluation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EvaluationType evaluationType;

    @Column(name = "evaluator_id")
    private Long evaluatorId;

    @Column(name = "overall_score")
    private BigDecimal overallScore;

    @Column(name = "is_compliant", nullable = false)
    private Boolean isCompliant;

    @Column(name = "evaluation_notes", columnDefinition = "TEXT")
    private String evaluationNotes;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
