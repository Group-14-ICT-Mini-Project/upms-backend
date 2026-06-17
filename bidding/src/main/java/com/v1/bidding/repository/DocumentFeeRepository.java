package com.v1.bidding.repository;

import com.v1.bidding.model.DocumentFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentFeeRepository extends JpaRepository<DocumentFee, Long> {

    @Query("SELECT d FROM DocumentFee d WHERE d.bidId = :bidId")
    Optional<DocumentFee> findByBidId(@Param("bidId") Long bidId);

    @Query("SELECT d FROM DocumentFee d WHERE d.procurementId = :procurementId AND d.paymentStatus = 'PAID'")
    java.util.List<DocumentFee> findPaidFeesByProcurement(@Param("procurementId") Long procurementId);
}
