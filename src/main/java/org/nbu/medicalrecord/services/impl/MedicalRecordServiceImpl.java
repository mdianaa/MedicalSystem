package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.MedicalRecordDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicalRecordDtoResponse;
import org.nbu.medicalrecord.entities.MedicalRecord;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.repositories.MedicalRecordRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.MedicalRecordService;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

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
    public MedicalRecordDtoResponse showMedicalRecord(long patientId) {
        MedicalRecord rec = recordRepo.findByPatient_Id(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record for patient with id " + patientId + " not found"));
        return toDto(rec);
    }

    @Override
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
        return new MedicalRecordDtoResponse(
                rec.getId(),
                p.getId(),
                p.getUser().getFirstName(),
                p.getUser().getLastName(),
                p.getBirthDate(),
                rec.getVisits() == null ? 0 : rec.getVisits().size()
        );
    }
}

