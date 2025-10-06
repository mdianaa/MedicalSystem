package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEgn(String egn);
    boolean existsByEgn(String egn);
}
