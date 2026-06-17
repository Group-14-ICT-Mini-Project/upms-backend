package com.v1.procurement.repository;

import com.v1.procurement.model.Procurement;
import com.v1.procurement.model.ProcurementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcurementRepository extends JpaRepository<Procurement, Long> {

    Optional<Procurement> findByReferenceNumber(String referenceNumber);

    Page<Procurement> findByStatus(ProcurementStatus status, Pageable pageable);

    Page<Procurement> findByCreatedByUserId(Long userId, Pageable pageable);

    Page<Procurement> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT p FROM Procurement p WHERE p.closingDate < :date AND p.status = :status")
    List<Procurement> findClosingProcurements(@Param("date") LocalDateTime date, @Param("status") ProcurementStatus status);

    @Query("SELECT p FROM Procurement p WHERE p.estimatedValue BETWEEN :minValue AND :maxValue")
    Page<Procurement> findByEstimatedValueRange(@Param("minValue") BigDecimal minValue, 
                                                  @Param("maxValue") BigDecimal maxValue, 
                                                  Pageable pageable);

    @Query("SELECT p FROM Procurement p WHERE p.procurementMethod.id = :methodId AND p.isActive = true")
    List<Procurement> findByProcurementMethod(@Param("methodId") Long methodId);

    @Query("SELECT p FROM Procurement p WHERE p.status IN :statuses ORDER BY p.closingDate ASC")
    List<Procurement> findByStatusList(@Param("statuses") List<ProcurementStatus> statuses);
}
