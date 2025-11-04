package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.DiagnosisDtoRequest;
import org.nbu.medicalrecord.dtos.request.MedicationDtoRequest;
import org.nbu.medicalrecord.dtos.request.MedicineDtoRequest;
import org.nbu.medicalrecord.dtos.response.DiagnosisDtoResponse;
import org.nbu.medicalrecord.dtos.response.MedicationDtoResponse;
import org.nbu.medicalrecord.dtos.response.MedicineDtoResponse;
import org.nbu.medicalrecord.entities.*;
import org.nbu.medicalrecord.repositories.*;
import org.nbu.medicalrecord.services.DiagnosisService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosisServiceImpl implements DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final MedicineRepository medicineRepository;

    @Override
    @Transactional
    public DiagnosisDtoResponse createDiagnosis(DiagnosisDtoRequest req) {
        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor with id " + req.getDoctorId()+ " not found"));
        Patient patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient with id " + req.getPatientId() + " not found"));

        Diagnosis d = new Diagnosis();
        d.setDoctor(doctor);
        d.setPatient(patient);
        d.setComplaints(req.getComplaints());
        d.setMedicalHistory(req.getMedicalHistory());
        d.setDiagnosisResult(req.getDiagnosisResult());
        d.setRequiredTests(req.getRequiredTests());

        if (req.getMedication() != null) {
            MedicationDtoRequest medReq = req.getMedication();

            List<Medicine> medicines = medicineRepository.findAllById(medReq.getMedicineIds());
            if (medicines.size() != medReq.getMedicineIds().size()) {
                throw new IllegalArgumentException("Some medicineIds do not exist");
            }

            Medication medication = new Medication();
            medication.setMedicines(new java.util.HashSet<>(medicines));
            medication.setPrescription(medReq.getPrescription());
            d.setMedication(medication);
        }

        Diagnosis saved = diagnosisRepository.save(d);
        return toDto(saved);
    }

    @Override
    public Set<DiagnosisDtoResponse> showAllDiagnosisForPatientId(Long patientId) {
        patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient with id " + patientId + " not found"));

        return diagnosisRepository.findByPatient_Id(patientId).stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<DiagnosisDtoResponse> showAllDiagnosisByDoctorId(Long doctorId) {
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor with id " + doctorId + " not found"));

        return diagnosisRepository.findByDoctor_Id(doctorId).stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<DiagnosisDtoResponse> showMostFrequentDiagnosisResult() {
        List<DiagnosisRepository.DiagnosisResultCount> counts = diagnosisRepository.countByDiagnosisResultDesc();
        if (counts.isEmpty()) return Set.of();
        long max = counts.get(0).cnt();

        Set<String> topResults = counts.stream()
                .takeWhile(c -> c.cnt() == max)
                .map(DiagnosisRepository.DiagnosisResultCount::diagnosisResult)
                .collect(Collectors.toSet());

        // Return one sample row per top result
        return diagnosisRepository.findAll().stream()
                .filter(d -> topResults.contains(d.getDiagnosisResult()))
                .collect(Collectors.toMap(
                        Diagnosis::getDiagnosisResult, this::toDto, (a, b) -> a, LinkedHashMap::new
                ))
                .values().stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public void deleteDiagnosis(Long diagnosisId) {
        if (!diagnosisRepository.existsById(diagnosisId)) {
            throw new IllegalArgumentException("Diagnosis with id " + diagnosisId + " not found");
        }
        diagnosisRepository.deleteById(diagnosisId);
    }

    private DiagnosisDtoResponse toDto(Diagnosis d) {
        Doctor doc = d.getDoctor();
        Patient pat = d.getPatient();

        List<String> allergyNames = (d.getAllergies() == null) ? java.util.List.<String>of()
                : d.getAllergies().stream().map(Allergy::getAllergen).sorted().toList();

        MedicationDtoResponse medDto = null;
        if (d.getMedication() != null) {
            Medication med = d.getMedication();
            List<MedicineDtoResponse> meds = (med.getMedicines() == null ? List.of()
                    : med.getMedicines().stream()
                    .sorted(Comparator.comparing(Medicine::getName).thenComparing(Medicine::getMg))
                    .map(m -> new MedicineDtoResponse(
                            m.getId(),
                            m.getName(),
                            m.getMg(),
                            m.getMedicineType() != null ? m.getMedicineType().toString() : null
                    ))
                    .toList());
            medDto = new MedicationDtoResponse(
                    d.getId(),
                    d.getMedication().getPrescription(),
                    meds
            );
        }

        return new DiagnosisDtoResponse(
                d.getId(),
                doc != null ? (doc.getUser().getFirstName() + " " + doc.getUser().getLastName()) : null,
                pat != null ? (pat.getUser().getFirstName() + " " + pat.getUser().getLastName()) : null,
                d.getComplaints(),
                d.getMedicalHistory(),
                allergyNames,
                d.getDiagnosisResult(),
                medDto,
                d.getRequiredTests(),
                LocalDateTime.now()
        );
    }
}