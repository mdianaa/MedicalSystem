package org.nbu.medicalrecord.services.impl;

import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.MedicalRecordDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicalRecordDtoResponse;
import org.nbu.medicalrecord.dtos.response.VisitDtoResponse;
import org.nbu.medicalrecord.entities.*;
import org.nbu.medicalrecord.repositories.MedicalRecordRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.MedicalRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository recordRepo;
    private final PatientRepository patientRepo;

    @Override
    @Transactional
    public MedicalRecordDtoResponse createNewMedicalRecord(MedicalRecordDtoRequest req) {
        Patient patient = patientRepo.findById(req.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient with id " + req.getPatientId() + " not found"));

        if (recordRepo.existsByPatient_Id(patient.getId())) {
            throw new IllegalStateException("Medical record already exists for this patient.");
        }

        MedicalRecord rec = new MedicalRecord();
        rec.setPatient(patient);
        rec.setVisits(new HashSet<>());

        recordRepo.save(rec);
        return toDto(rec);
    }

    @Override
    @Transactional
    public MedicalRecordDtoResponse showMedicalRecord(long patientId) {
        MedicalRecord rec = recordRepo.findByPatient_Id(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record for patient with id " + patientId + " not found"));
        return toDto(rec);
    }

    @Override
    @Transactional
    public Set<MedicalRecordDtoResponse> showAllMedicalRecords() {
        return recordRepo.findAll().stream()
                .map(this::toDto)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    @Override
    @Transactional
    public void deleteMedicalRecord(long medicalRecordId) {
        if (!recordRepo.existsById(medicalRecordId)) {
            throw new IllegalArgumentException("Medical record with id " + medicalRecordId + " not found");
        }
        recordRepo.deleteById(medicalRecordId);
    }

    private MedicalRecordDtoResponse toDto(MedicalRecord rec) {
        Patient p = rec.getPatient();

        Set<VisitDtoResponse> visitDtos = rec.getVisits() == null ? Set.of()
                : rec.getVisits().stream()
                .map(this::toVisitDto)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

        MedicalRecordDtoResponse dto = new MedicalRecordDtoResponse();
        dto.setId(rec.getId());
        dto.setPatientId(p.getId());
        dto.setFirstName(p.getUser().getFirstName());
        dto.setLastName(p.getUser().getLastName());
        dto.setBirthDate(p.getBirthDate());
        dto.setVisitsCount(visitDtos.size());
        dto.setVisits(visitDtos);
        return dto;
    }

    private VisitDtoResponse toVisitDto(Visit v) {
        Appointment appt = v.getAppointment();
        Doctor doc = (appt != null) ? appt.getDoctor() : null;
        User docUser = (doc != null) ? doc.getUser() : null;

        MedicalRecord mr = v.getMedicalRecord();

        VisitDtoResponse dto = new VisitDtoResponse();
        dto.setId(v.getId());
        dto.setDoctorName(docUser != null ? docUser.getFirstName() + " " + docUser.getLastName() : null);
        dto.setAppointmentId(appt != null ? appt.getId() : null);
        dto.setMedicalRecordId(mr != null ? mr.getId() : null);
        dto.setDiagnosisId(v.getDiagnosis() != null ? v.getDiagnosis().getId() : null);
        dto.setSickLeaveId(v.getSickLeave() != null ? v.getSickLeave().getId() : null);
        return dto;
    }
}

