package com.v1.bidding.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vat_sscl_declarations", indexes = {
        @Index(name = "idx_bid_id", columnList = "bid_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VatSsclDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bid_id", nullable = false)
    private Long bidId;

    @Column(name = "registration_number", nullable = false)
    private String registrationNumber;

    @Column(name = "registration_type", nullable = false)
    private String registrationType;

    @Column(name = "is_declared", nullable = false)
    private Boolean isDeclared;

    @Column(name = "declaration_date")
    private LocalDate declarationDate;

    @Column(name = "verification_status")
    private String verificationStatus;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        verificationStatus = "PENDING";
    }
}
