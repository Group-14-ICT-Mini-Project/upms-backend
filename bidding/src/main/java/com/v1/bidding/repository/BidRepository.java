package com.v1.bidding.repository;

import com.v1.bidding.model.Bid;
import com.v1.bidding.model.BidStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    Optional<Bid> findByBidReference(String bidReference);

    @Query("SELECT b FROM Bid b WHERE b.procurementId = :procurementId")
    Page<Bid> findByProcurementId(@Param("procurementId") Long procurementId, Pageable pageable);

    @Query("SELECT b FROM Bid b WHERE b.bidderId = :bidderId")
    Page<Bid> findByBidderId(@Param("bidderId") Long bidderId, Pageable pageable);

    @Query("SELECT b FROM Bid b WHERE b.procurementId = :procurementId AND b.bidStatus = :status")
    List<Bid> findByProcurementIdAndStatus(@Param("procurementId") Long procurementId, 
                                            @Param("status") BidStatus status);

    @Query("SELECT b FROM Bid b WHERE b.procurementId = :procurementId AND b.isCompliant = true")
    List<Bid> findCompliantBids(@Param("procurementId") Long procurementId);

    @Query("SELECT b FROM Bid b WHERE b.procurementId = :procurementId ORDER BY b.financialBid ASC")
    List<Bid> findBidsByProcurementOrderedByPrice(@Param("procurementId") Long procurementId);
}
