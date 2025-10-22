package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDoctor_IdAndDateAndHourOfAppointment(Long doctorId, LocalDate date, LocalTime hour);

    boolean existsByIdAndDoctor_Id(Long appointmentId, Long doctorId);

    Optional<Appointment> findByPatient_IdAndDate(Long patientId, LocalDate date);

    List<Appointment> findByDoctor_IdAndPatientIsNullAndDateGreaterThanEqual(Long doctorId, LocalDate fromDate);

    List<Appointment> findByPatient_Id(Long patientId);

    List<Appointment> findByDoctor_IdAndPatientIsNotNull(Long doctorId);
}
