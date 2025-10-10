package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    Optional<MedicalRecord> findByPatient_Id(Long patientId);

    boolean existsByPatient_Id(Long patientId);
}
