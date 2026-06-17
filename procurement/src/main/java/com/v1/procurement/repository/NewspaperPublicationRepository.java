package com.v1.procurement.repository;

import com.v1.procurement.model.NewspaperPublication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewspaperPublicationRepository extends JpaRepository<NewspaperPublication, Long> {

    @Query("SELECT n FROM NewspaperPublication n WHERE n.procurement.id = :procurementId")
    List<NewspaperPublication> findByProcurementId(@Param("procurementId") Long procurementId);

    @Query("SELECT n FROM NewspaperPublication n WHERE n.procurement.id = :procurementId AND n.isVerified = true")
    List<NewspaperPublication> findVerifiedByProcurementId(@Param("procurementId") Long procurementId);
}
