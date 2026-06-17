package com.v1.procurement.repository;

import com.v1.procurement.model.ProcurementCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcurementCategoryRepository extends JpaRepository<ProcurementCategory, Long> {

    Optional<ProcurementCategory> findByCategoryCode(String categoryCode);

    Optional<ProcurementCategory> findByCategoryName(String categoryName);
}
