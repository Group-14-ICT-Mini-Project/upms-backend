package com.v1.procurement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "newspaper_publications", indexes = {
        @Index(name = "idx_procurement_id", columnList = "procurement_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewspaperPublication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procurement_id", nullable = false)
    private Procurement procurement;

    @Column(name = "newspaper_name", nullable = false)
    private String newspaperName;

    @Column(name = "publication_date", nullable = false)
    private LocalDate publicationDate;

    @Column(name = "language")
    private String language;

    @Column(name = "page_number")
    private String pageNumber;

    @Column(name = "publication_reference")
    private String publicationReference;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;
}
