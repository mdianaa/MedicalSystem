package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    boolean existsByAppointment_Id(Long appointmentId);

    boolean existsByDiagnosis_Id(Long diagnosisId);

    // doctor’s visits (by appointment.doctor)
    List<Visit> findByAppointment_Doctor_IdOrderByAppointment_DateDesc(Long doctorId);

    // patient’s visits (by medicalRecord.patient)
    List<Visit> findByMedicalRecord_Patient_IdOrderByAppointment_DateDesc(Long patientId);

    List<Visit> findByMedicalRecord_Patient_IdAndAppointment_DateBetweenOrderByAppointment_DateDesc(
            Long patientId, LocalDate from, LocalDate to);

    List<Visit> findByAppointment_Doctor_IdAndAppointment_DateBetweenOrderByAppointment_DateDesc(
            Long doctorId, LocalDate from, LocalDate to);

}
