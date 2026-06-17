package com.v1.bidding.repository;

import com.v1.bidding.model.BidBond;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BidBondRepository extends JpaRepository<BidBond, Long> {

    Optional<BidBond> findByBondReference(String bondReference);

    @Query("SELECT b FROM BidBond b WHERE b.bidId = :bidId")
    Optional<BidBond> findByBidId(@Param("bidId") Long bidId);
}
