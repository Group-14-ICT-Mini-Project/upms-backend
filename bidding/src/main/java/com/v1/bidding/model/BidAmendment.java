package com.v1.bidding.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bid_amendments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidAmendment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bid_id", nullable = false)
    private Long bidId;

    @Column(name = "amendment_type", nullable = false)
    private String amendmentType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "amendment_status")
    private String amendmentStatus;

    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        amendmentStatus = "SUBMITTED";
        approvalStatus = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
