package com.v1.procurement.repository;

import com.v1.procurement.model.RfqRecipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RfqRecipientRepository extends JpaRepository<RfqRecipient, Long> {

    @Query("SELECT r FROM RfqRecipient r WHERE r.procurement.id = :procurementId")
    List<RfqRecipient> findByProcurementId(@Param("procurementId") Long procurementId);

    @Query("SELECT r FROM RfqRecipient r WHERE r.procurement.id = :procurementId AND r.responseStatus = :status")
    Page<RfqRecipient> findByProcurementIdAndResponseStatus(@Param("procurementId") Long procurementId, 
                                                              @Param("status") String status, 
                                                              Pageable pageable);

    @Query("SELECT r FROM RfqRecipient r WHERE r.supplierEmail = :email AND r.procurement.id = :procurementId")
    List<RfqRecipient> findBySupplierEmailAndProcurementId(@Param("email") String email, 
                                                              @Param("procurementId") Long procurementId);
}
