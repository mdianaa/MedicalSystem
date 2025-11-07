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

import static org.nbu.medicalrecord.util.CheckExistUtil.*;

@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;
    private final AppointmentRepository appointmentRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final MedicationRepository medicationRepository;
    private final SickLeaveRepository sickLeaveRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public VisitDtoResponse createNewVisit(VisitDtoRequest req) {
        var appt = appointmentRepository.findById(req.getAppointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment with id " + req.getAppointmentId() + " not found"));

        if (visitRepository.existsByAppointment_Id(appt.getId())) {
            throw new IllegalStateException("A visit already exists for this appointment.");
        }
        if (appt.getPatient() == null) {
            throw new IllegalStateException("Appointment is not booked by a patient.");
        }

        var doctor  = appt.getDoctor();
        var patient = appt.getPatient();

        var record = medicalRecordRepository.findById(req.getMedicalRecordId())
                .orElseThrow(() -> new IllegalArgumentException("Medical record with id " + req.getMedicalRecordId() + " not found"));
        if (!Objects.equals(record.getPatient().getId(), patient.getId())) {
            throw new IllegalStateException("Medical record must belong to the same patient as the appointment.");
        }

        Diagnosis diagnosis = null;
        if (req.getDiagnosisId() != null) {
            diagnosis = diagnosisRepository.findById(req.getDiagnosisId())
                    .orElseThrow(() -> new IllegalArgumentException("Diagnosis with id " + req.getDiagnosisId() + " not found"));
            if (!Objects.equals(diagnosis.getDoctor().getId(), doctor.getId())
                    || !Objects.equals(diagnosis.getPatient().getId(), patient.getId())) {
                throw new IllegalStateException("Diagnosis must belong to the same doctor and patient as the appointment.");
            }
        }

        SickLeave sickLeave = null;
        if (req.getSickLeaveId() != null) {
            sickLeave = sickLeaveRepository.findById(req.getSickLeaveId())
                    .orElseThrow(() -> new IllegalArgumentException("Sick leave with id " + req.getSickLeaveId() + " not found"));
            if (!Objects.equals(sickLeave.getDoctor().getId(), doctor.getId())
                    || !Objects.equals(sickLeave.getPatient().getId(), patient.getId())) {
                throw new IllegalStateException("Sick leave must belong to the same doctor and patient.");
            }
        }

        Medication medication = null;
        if (req.getMedicationId() != null) {
            medication = medicationRepository.findById(req.getMedicationId())
                    .orElseThrow(() -> new IllegalArgumentException("Medication with id " + req.getMedicationId() + " not found"));
        }

        var v = new Visit();
        v.setAppointment(appt);
        v.setMedicalRecord(record);
        v.setDiagnosis(diagnosis);
        v.setMedication(medication);
        v.setSickLeave(sickLeave);

        v.setComplaints(req.getComplaints());
        v.setMedicalHistory(req.getMedicalHistory());
        v.setRequiredTests(req.getRequiredTests());

        visitRepository.save(v);
        return toDto(v);
    }

    @Override
    public Set<VisitDtoResponse> showAllVisitsByDoctor(long doctorId) {
        checkIfDoctorExists(doctorRepository, doctorId);

        return visitRepository.findByAppointment_Doctor_IdOrderByAppointment_DateDesc(doctorId).stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<VisitDtoResponse> showAllVisitsForPatient(long patientId) {
        checkIfPatientExists(patientRepository, patientId);

        return visitRepository.findByMedicalRecord_Patient_IdOrderByAppointment_DateDesc(patientId).stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<VisitDtoResponse> showAllVisitsForPatientInPeriod(long patientId, LocalDate from, LocalDate to) {
        checkIfPatientExists(patientRepository, patientId);
        validateRange(from, to);

        return visitRepository
                .findByMedicalRecord_Patient_IdAndAppointment_DateBetweenOrderByAppointment_DateDesc(patientId, from, to)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<VisitDtoResponse> showAllVisitsByDoctorInPeriod(long doctorId, LocalDate from, LocalDate to) {
        checkIfDoctorExists(doctorRepository, doctorId);
        validateRange(from, to);

        return visitRepository
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

        var dto = new VisitDtoResponse();
        dto.setId(v.getId());
        dto.setDoctorName(doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName());
        dto.setAppointmentId(appt != null ? appt.getId() : null);
        dto.setMedicalRecordId(record != null ? record.getId() : null);
        dto.setSickLeaveId(v.getSickLeave() != null ? v.getSickLeave().getId() : null);
        dto.setMedicationId(v.getMedication() != null ? v.getMedication().getId() : null);
        dto.setDiagnosisId(v.getDiagnosis() != null ? v.getDiagnosis().getId() : null);
        dto.setComplaints(v.getComplaints());
        dto.setMedicalHistory(v.getMedicalHistory());
        dto.setRequiredTests(v.getRequiredTests());
        return dto;
    }
}