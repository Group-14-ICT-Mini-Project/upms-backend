package com.v1.procurement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promise_lk_posts", indexes = {
        @Index(name = "idx_procurement_id", columnList = "procurement_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromiseLkPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procurement_id", nullable = false)
    private Procurement procurement;

    @Column(name = "promise_lk_post_id")
    private String promiseLkPostId;

    @Column(name = "posting_date", nullable = false)
    private LocalDate postingDate;

    @Column(name = "removal_date")
    private LocalDate removalDate;

    @Column(name = "posting_status")
    private String postingStatus;

    @Column(name = "promise_lk_url")
    private String promiseLkUrl;

    @Column(name = "posting_verification_date")
    private LocalDateTime postingVerificationDate;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;
}
