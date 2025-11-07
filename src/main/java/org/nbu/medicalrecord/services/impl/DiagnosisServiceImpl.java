package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.DiagnosisDtoRequest;
import org.nbu.medicalrecord.dtos.response.DiagnosisDtoResponse;
import org.nbu.medicalrecord.entities.*;
import org.nbu.medicalrecord.repositories.*;
import org.nbu.medicalrecord.services.DiagnosisService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.nbu.medicalrecord.util.CheckExistUtil.*;

@Service
@RequiredArgsConstructor
public class DiagnosisServiceImpl implements DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;

    @Override
    @Transactional
    public DiagnosisDtoResponse createDiagnosis(DiagnosisDtoRequest req) {
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient with id " + req.getPatientId() + " not found"));

        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor with id " + req.getDoctorId() + " not found"));

        Diagnosis d = new Diagnosis();
        d.setDiagnosis(req.getDiagnosis());
        d.setPatient(patient);
        d.setDoctor(doctor);

        Diagnosis saved = diagnosisRepository.save(d);
        return toDto(saved);
    }

    @Override
    @Transactional
    public Set<DiagnosisDtoResponse> showAllDiagnosisForPatientId(Long patientId) {
        checkIfPatientExists(patientRepository, patientId);

        return diagnosisRepository.findByPatient_Id(patientId).stream()
                .sorted(Comparator.comparing(Diagnosis::getId))
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public Set<DiagnosisDtoResponse> showAllDiagnosisByDoctorId(Long doctorId) {
        checkIfDoctorExists(doctorRepository, doctorId);

        return diagnosisRepository.findByDoctor_Id(doctorId).stream()
                .sorted(java.util.Comparator.comparing(Diagnosis::getId))
                .map(this::toDto)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }


    @Override
    @Transactional
    public Set<DiagnosisDtoResponse> showMostFrequentDiagnosisResult() {
        List<DiagnosisRepository.DiagnosisResultCount> counts = diagnosisRepository.countByDiagnosisResultDesc();
        if (counts.isEmpty()) return Set.of();

        long max = counts.getFirst().cnt();

        Set<String> topResults = counts.stream()
                .takeWhile(c -> c.cnt() == max)
                .map(DiagnosisRepository.DiagnosisResultCount::diagnosisResult)
                .collect(Collectors.toSet());

        return new LinkedHashSet<>(diagnosisRepository.findAll().stream()
                .filter(d -> topResults.contains(d.getDiagnosis()))
                .sorted(Comparator.comparing(Diagnosis::getId))
                .collect(Collectors.toMap(
                        Diagnosis::getDiagnosis, this::toDto, (a, b) -> a, LinkedHashMap::new
                ))
                .values());
    }

    @Override
    @Transactional
    public void deleteDiagnosis(Long diagnosisId) {
        if (!diagnosisRepository.existsById(diagnosisId)) {
            throw new IllegalArgumentException("Diagnosis with id " + diagnosisId + " not found");
        }

        boolean usedByVisit = visitRepository.existsByDiagnosis_Id(diagnosisId);
        if (usedByVisit) {
            throw new IllegalStateException(
                    "Cannot delete diagnosis " + diagnosisId + " because it is referenced by a visit."
            );
        }

        diagnosisRepository.deleteById(diagnosisId);
    }

    private DiagnosisDtoResponse toDto(Diagnosis d) {
        DiagnosisDtoResponse dto = new DiagnosisDtoResponse();
        dto.setId(d.getId());
        dto.setDiagnosis(d.getDiagnosis());
        dto.setPatientId(d.getPatient() != null ? d.getPatient().getId() : null);
        if (d.getDoctor() != null) {
            dto.setDoctorId(d.getDoctor().getId());
            User u = d.getDoctor().getUser();
            dto.setDoctorName(u != null ? (u.getFirstName() + " " + u.getLastName()) : null);
        }
        return dto;
    }
}