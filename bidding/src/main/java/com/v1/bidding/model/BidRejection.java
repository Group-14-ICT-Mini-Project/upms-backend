package com.v1.bidding.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bid_rejections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidRejection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bid_id", nullable = false)
    private Long bidId;

    @Column(name = "rejection_reason", columnDefinition = "TEXT", nullable = false)
    private String rejectionReason;

    @Column(name = "rejection_category")
    private String rejectionCategory;

    @Column(name = "appeals_allowed", nullable = false)
    private Boolean appealsAllowed;

    @Column(name = "appeal_deadline")
    private LocalDateTime appealDeadline;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        appealsAllowed = true;
        appealDeadline = LocalDateTime.now().plusDays(7);
    }
}
