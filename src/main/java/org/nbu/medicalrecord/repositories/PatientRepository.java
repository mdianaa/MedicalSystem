package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByUser_Egn(String egn);

    boolean existsByIdAndGp_Id(Long patientId, Long doctorId);

    boolean existsByAllergies_Id(Long allergyId);

    // Patients registered with a specific GP
    List<Patient> findByGp_Id(Long doctorId);

    Optional<Patient> findByUser_Email(String email);

    int countByGp_Id(Long doctorId);

    // Patients who have at least one diagnosis created by doctor
    @Query("""
      select distinct d.patient
      from Diagnosis d
      where d.doctor.id = :doctorId
    """)
    List<Patient> findDistinctByVisitedDoctor(Long doctorId);

    @Query("""
      select count(distinct d.patient.id)
      from Diagnosis d
      where d.doctor.id = :doctorId
    """)
    int countDistinctByVisitedDoctor(Long doctorId);

    // Patients with a specific diagnosis result
    @Query("""
      select distinct d.patient
      from Diagnosis d
      where d.diagnosis = :result
    """)
    List<Patient> findDistinctByDiagnosis(String diagnosis);

    @Query("""
      select count(distinct d.patient.id)
      from Diagnosis d
      where d.diagnosis = :result
    """)
    int countDistinctByDiagnosis(String diagnosis);

    // Patients with at least one allergy (by allergen name)
    @Query("""
    select distinct p
    from Patient p
    join p.allergies a
    where lower(a.allergen) = lower(:allergen)
    """)
    List<Patient> findDistinctByAllergen(String allergen);
}
