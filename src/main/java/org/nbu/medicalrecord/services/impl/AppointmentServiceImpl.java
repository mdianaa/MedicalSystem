package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.AppointmentDtoRequest;
import org.nbu.medicalrecord.dtos.response.DoctorAppointmentDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientAppointmentDtoResponse;
import org.nbu.medicalrecord.entities.Appointment;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.repositories.AppointmentRepository;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.AppointmentService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public PatientAppointmentDtoResponse makeAppointment(AppointmentDtoRequest req) {
        if (req.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot make appointments in the past.");
        }

        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor with id " + req.getDoctorId() + " not found"));
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient with id " + req.getPatientId() + " not found"));

        if (appointmentRepository.existsByDoctor_IdAndDateAndHourOfAppointment(
                doctor.getId(), req.getDate(), req.getHourOfAppointment())) {
            throw new IllegalStateException("This slot is already booked.");
        }

        Appointment a = new Appointment();
        a.setDoctor(doctor);
        a.setPatient(patient);
        a.setDate(req.getDate());
        a.setHourOfAppointment(req.getHourOfAppointment());

        try {
            appointmentRepository.save(a);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("This slot was just booked by someone else.", e);
        }

        return toPatientDto(a);
    }

    @Override
    @Transactional
    public Set<PatientAppointmentDtoResponse> showAllAvailableAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository
                .findByDoctor_IdAndPatientIsNullAndDateGreaterThanEqual(doctorId, LocalDate.now())
                .stream()
                .sorted(Comparator.comparing(Appointment::getDate)
                        .thenComparing(Appointment::getHourOfAppointment))
                .map(this::toPatientDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public Set<PatientAppointmentDtoResponse> showAllPatientAppointmentsById(Long patientId) {
        if (patientRepository.findById(patientId).isEmpty()) {
            throw new IllegalArgumentException("Patient with id " + patientId + " not found");
        }

        return appointmentRepository.findByPatient_Id(patientId).stream()
                .sorted(Comparator.comparing(Appointment::getDate)
                        .thenComparing(Appointment::getHourOfAppointment))
                .map(this::toPatientDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public Set<PatientAppointmentDtoResponse> showPatientAppointmentOnDateById(Long patientId, LocalDate date) {
        if (patientRepository.findById(patientId).isEmpty()) {
            throw new IllegalArgumentException("Patient with id " + patientId + " not found");
        }

        List<Appointment> appts = appointmentRepository.findByPatient_IdAndDate(patientId, date);
        if (appts.isEmpty()) {
            throw new IllegalArgumentException("No appointments for patient %d on %s".formatted(patientId, date));
        }

        return appts.stream()
                .sorted(Comparator.comparing(Appointment::getDate)
                        .thenComparing(Appointment::getHourOfAppointment))
                .map(this::toPatientDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public Set<DoctorAppointmentDtoResponse> showAllOccupiedAppointmentsById(Long doctorId) {
        if (doctorRepository.findById(doctorId).isEmpty()) {
            throw new IllegalArgumentException("Doctor with id " + doctorId + " not found");
        }

        return appointmentRepository.findByDoctor_IdAndPatientIsNotNull(doctorId).stream()
                .sorted(Comparator.comparing(Appointment::getDate)
                        .thenComparing(Appointment::getHourOfAppointment))
                .map(this::toDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public void cancelAppointment(long appointmentId) {
        appointmentRepository.deleteById(appointmentId);
    }

    private PatientAppointmentDtoResponse toPatientDto(Appointment a) {
        Doctor d = a.getDoctor();
        String doctorName = d == null ? null : d.getUser().getFirstName() + " " + d.getUser().getLastName();
        return new PatientAppointmentDtoResponse(
                a.getId(),
                a.getPatient() != null ? a.getPatient().getId() : null,
                d != null ? d.getId() : null,
                doctorName,
                a.getDate(),
                a.getHourOfAppointment()
        );
    }

    private DoctorAppointmentDtoResponse toDoctorDto(Appointment a) {
        Patient p = a.getPatient();
        String patientName = p == null ? null : p.getUser().getFirstName() + " " + p.getUser().getLastName();
        return new DoctorAppointmentDtoResponse(
                a.getId(),
                p != null ? p.getId() : null,
                patientName,
                a.getDate(),
                a.getHourOfAppointment()
        );
    }
}