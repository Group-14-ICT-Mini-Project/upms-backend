package com.v1.procurement.repository;

import com.v1.procurement.model.ProcurementMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcurementMethodRepository extends JpaRepository<ProcurementMethod, Long> {

    Optional<ProcurementMethod> findByMethodCode(String methodCode);

    Optional<ProcurementMethod> findByMethodName(String methodName);
}
