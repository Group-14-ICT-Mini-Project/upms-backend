package com.v1.bidding.repository;

import com.v1.bidding.model.BidAmendment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidAmendmentRepository extends JpaRepository<BidAmendment, Long> {

    @Query("SELECT ba FROM BidAmendment ba WHERE ba.bidId = :bidId ORDER BY ba.createdDate DESC")
    List<BidAmendment> findByBidId(@Param("bidId") Long bidId);

    @Query("SELECT ba FROM BidAmendment ba WHERE ba.bidId = :bidId AND ba.approvalStatus = :status")
    List<BidAmendment> findByBidIdAndApprovalStatus(@Param("bidId") Long bidId, @Param("status") String status);
}
