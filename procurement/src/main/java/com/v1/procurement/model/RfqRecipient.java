package com.v1.procurement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rfq_recipients", indexes = {
        @Index(name = "idx_procurement_id", columnList = "procurement_id"),
        @Index(name = "idx_supplier_email", columnList = "supplier_email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfqRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procurement_id", nullable = false)
    private Procurement procurement;

    @Column(name = "supplier_email", nullable = false)
    private String supplierEmail;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "supplier_id")
    private String supplierId;

    @Column(name = "invitation_sent_date")
    private LocalDateTime invitationSentDate;

    @Column(name = "invitation_status")
    private String invitationStatus;

    @Column(name = "response_status")
    private String responseStatus;

    @Column(name = "bid_submitted_date")
    private LocalDateTime bidSubmittedDate;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        invitationStatus = "PENDING";
        responseStatus = "NOT_RESPONDED";
    }
}
