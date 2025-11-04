package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.VisitDtoRequest;
import org.nbu.medicalrecord.dtos.response.VisitDtoResponse;
import org.nbu.medicalrecord.entities.*;
import org.nbu.medicalrecord.repositories.*;
import org.nbu.medicalrecord.services.VisitService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepo;
    private final AppointmentRepository appointmentRepo;
    private final DiagnosisRepository diagnosisRepo;
    private final SickLeaveRepository sickLeaveRepo;
    private final MedicalRecordRepository medicalRecordRepo;

    @Override
    @Transactional
    public VisitDtoResponse createNewVisit(VisitDtoRequest req) {
        var appt = appointmentRepo.findById(req.getAppointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment with id " + req.getAppointmentId() + " not found"));

        if (visitRepo.existsByAppointment_Id(appt.getId())) {
            throw new IllegalStateException("A visit already exists for this appointment.");
        }
        if (appt.getPatient() == null) {
            throw new IllegalStateException("Appointment is not booked by a patient.");
        }

        var doctor  = appt.getDoctor();
        var patient = appt.getPatient();

        var record = medicalRecordRepo.findByPatient_Id(patient.getId())
                .orElseThrow(() -> new IllegalArgumentException("Medical record for patient with id " + patient.getId() + " not found"));

        Diagnosis diagnosis = null;
        if (req.getDiagnosisId() != null) {
            diagnosis = diagnosisRepo.findById(req.getDiagnosisId())
                    .orElseThrow(() -> new IllegalArgumentException("Diagnosis with id " + req.getDiagnosisId() + " not found"));
            if (!Objects.equals(diagnosis.getDoctor().getId(), doctor.getId())
                    || !Objects.equals(diagnosis.getPatient().getId(), patient.getId())) {
                throw new IllegalStateException("Diagnosis must belong to the same doctor and patient.");
            }
        }

        SickLeave sickLeave = null;
        if (req.getSickLeaveId() != null) {
            sickLeave = sickLeaveRepo.findById(req.getSickLeaveId())
                    .orElseThrow(() -> new IllegalArgumentException("Sick leave with id " + req.getSickLeaveId() + " not found"));
            if (!Objects.equals(sickLeave.getDoctor().getId(), doctor.getId())
                    || !Objects.equals(sickLeave.getPatient().getId(), patient.getId())) {
                throw new IllegalStateException("Sick leave must belong to the same doctor and patient.");
            }
        }

        var v = new Visit();
        v.setAppointment(appt);
        v.setDiagnosis(diagnosis);
        v.setSickLeave(sickLeave);
        v.setMedicalRecord(record);

        visitRepo.save(v);
        return toDto(v);
    }

    @Override
    public Set<VisitDtoResponse> showAllVisitsByDoctor(long doctorId) {
        return visitRepo.findByAppointment_Doctor_IdOrderByAppointment_DateDesc(doctorId).stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<VisitDtoResponse> showAllVisitsForPatient(long patientId) {
        return visitRepo.findByMedicalRecord_Patient_IdOrderByAppointment_DateDesc(patientId).stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<VisitDtoResponse> showAllVisitsForPatientInPeriod(long patientId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        return visitRepo
                .findByMedicalRecord_Patient_IdAndAppointment_DateBetweenOrderByAppointment_DateDesc(patientId, from, to)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<VisitDtoResponse> showAllVisitsByDoctorInPeriod(long doctorId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        return visitRepo
                .findByAppointment_Doctor_IdAndAppointment_DateBetweenOrderByAppointment_DateDesc(doctorId, from, to)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("'from' and 'to' must be provided");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must be on or before 'to'");
        }
    }

    private VisitDtoResponse toDto(Visit v) {
        var appt = v.getAppointment();
        var doctor = appt != null ? appt.getDoctor() : null;
        var record = v.getMedicalRecord();
        var patient = record != null ? record.getPatient() : null;

        String doctorName = (doctor != null && doctor.getUser() != null)
                ? doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName()
                : null;
        String patientName = (patient != null && patient.getUser() != null)
                ? patient.getUser().getFirstName() + " " + patient.getUser().getLastName()
                : null;

        return new VisitDtoResponse(
                v.getId(),
                appt != null ? appt.getId() : null,
                doctor != null ? doctor.getId() : null,
                doctorName,
                patient != null ? patient.getId() : null,
                patientName,
                record != null ? record.getId() : null,
                appt != null ? appt.getDate() : null,
                appt != null ? appt.getHourOfAppointment() : null,
                v.getDiagnosis() != null ? v.getDiagnosis().getId() : null,
                v.getSickLeave() != null ? v.getSickLeave().getId() : null
        );
    }
}