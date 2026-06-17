package com.v1.bidding.repository;

import com.v1.bidding.model.VatSsclDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VatSsclDeclarationRepository extends JpaRepository<VatSsclDeclaration, Long> {

    @Query("SELECT v FROM VatSsclDeclaration v WHERE v.bidId = :bidId")
    Optional<VatSsclDeclaration> findByBidId(@Param("bidId") Long bidId);

    @Query("SELECT v FROM VatSsclDeclaration v WHERE v.registrationNumber = :regNumber")
    Optional<VatSsclDeclaration> findByRegistrationNumber(@Param("regNumber") String regNumber);
}
