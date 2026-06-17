package com.v1.bidding.repository;

import com.v1.bidding.model.BidRejection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BidRejectionRepository extends JpaRepository<BidRejection, Long> {

    @Query("SELECT br FROM BidRejection br WHERE br.bidId = :bidId")
    Optional<BidRejection> findByBidId(@Param("bidId") Long bidId);
}
