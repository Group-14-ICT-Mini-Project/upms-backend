package com.v1.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum RoleEnum {
        ADMIN,
        PROCUREMENT_OFFICER,
        BIDDER,
        EVALUATOR,
        TENDER_BOARD_MEMBER,
        HOD,
        FACULTY_BURSAR,
        FACULTY_DEAN,
        BURSAR,
        SUPPLIER_DIVISION_CLERK,
        TEC_MEMBER,
        STORE_KEEPER,
        FINANCE_DIVISION
    }
}
