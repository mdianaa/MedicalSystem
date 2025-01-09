package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.HealthInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthInsuranceRepository extends JpaRepository<HealthInsurance, Long> {
}
