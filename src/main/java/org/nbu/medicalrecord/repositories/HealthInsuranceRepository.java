package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.HealthInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Month;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthInsuranceRepository extends JpaRepository<HealthInsurance, Long> {

    boolean existsByPatient_IdAndMonthAndYear(Long patientId, Month month, int year);

    Optional<HealthInsurance> findByPatient_IdAndMonthAndYear(Long patientId, Month month, int year);

    List<HealthInsurance> findByPatient_IdAndYearOrderByMonthAsc(Long patientId, int year);

    // Last 6 months for a patient, crossing year boundaries (requires numeric year)
    @Query("""
      select hi from HealthInsurance hi
      where hi.patient.id = :patientId
      order by hi.year desc, hi.month desc
    """)
    List<HealthInsurance> findAllByPatientOrderByYearMonthDesc(Long patientId);
}
