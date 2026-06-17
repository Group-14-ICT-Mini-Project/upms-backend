package com.v1.bidding.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bid_bonds", indexes = {
        @Index(name = "idx_bid_id", columnList = "bid_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidBond {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bid_id", nullable = false)
    private Long bidId;

    @Column(name = "bond_reference", unique = true, nullable = false)
    private String bondReference;

    @Column(name = "issuer_bank", nullable = false)
    private String issuerBank;

    @Column(name = "bond_amount", nullable = false)
    private BigDecimal bondAmount;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "is_valid", nullable = false)
    private Boolean isValid;

    @Column(name = "validation_status")
    private String validationStatus;

    @Column(name = "validation_date")
    private LocalDate validationDate;

    @Column(name = "created_date", nullable = false, updatable = false)
    private java.time.LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = java.time.LocalDateTime.now();
        validationStatus = "PENDING";
    }
}
