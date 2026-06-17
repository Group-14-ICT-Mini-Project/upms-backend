package com.v1.procurement.repository;

import com.v1.procurement.model.PromiseLkPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromiseLkPostRepository extends JpaRepository<PromiseLkPost, Long> {

    @Query("SELECT p FROM PromiseLkPost p WHERE p.procurement.id = :procurementId")
    List<PromiseLkPost> findByProcurementId(@Param("procurementId") Long procurementId);

    @Query("SELECT p FROM PromiseLkPost p WHERE p.procurement.id = :procurementId AND p.isVerified = true")
    List<PromiseLkPost> findVerifiedByProcurementId(@Param("procurementId") Long procurementId);
}
