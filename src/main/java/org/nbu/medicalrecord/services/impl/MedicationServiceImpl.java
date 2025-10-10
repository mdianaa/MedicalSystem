package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.MedicationDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicationDtoResponse;
import org.nbu.medicalrecord.dtos.response.MedicineDtoResponse;
import org.nbu.medicalrecord.entities.Medication;
import org.nbu.medicalrecord.entities.Medicine;
import org.nbu.medicalrecord.repositories.DiagnosisRepository;
import org.nbu.medicalrecord.repositories.MedicationRepository;
import org.nbu.medicalrecord.repositories.MedicineRepository;
import org.nbu.medicalrecord.services.MedicationService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicineRepository medicineRepository;
    private final DiagnosisRepository diagnosisRepository; // used for "by doctor" view

    @Override
    @Transactional
    public MedicationDtoResponse addMedication(MedicationDtoRequest req) {
        List<Medicine> meds = medicineRepository.findAllById(req.getMedicineIds());
        if (meds.size() != req.getMedicineIds().size()) {
            throw new IllegalArgumentException("Some medicineIds do not exist");
        }
        Medication medication = new Medication();
        medication.setMedicines(new HashSet<>(meds));
        medication.setPrescription(req.getPrescription());
        medicationRepository.save(medication);
        return toDto(medication);
    }

    @Override
    public Set<MedicationDtoResponse> showAllMedications() {
        return medicationRepository.findAll().stream().map(this::toDto).collect(Collectors.toSet());
    }

    @Override
    public Set<MedicationDtoResponse> showAllMedicationsByDoctor(long doctorId) {
        return diagnosisRepository.findByDoctor_Id(doctorId).stream()
                .map(d -> d.getMedication())
                .filter(Objects::nonNull)
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private MedicationDtoResponse toDto(Medication medication) {
        List<MedicineDtoResponse> meds = Optional.ofNullable(medication.getMedicines())
                .orElseGet(Set::of)
                .stream()
                .sorted(Comparator.comparing(Medicine::getName).thenComparing(Medicine::getMg))
                .map(m -> new MedicineDtoResponse(
                        m.getId(), m.getName(), m.getMg(),
                        m.getMedicineType() != null ? m.getMedicineType().toString() : null
                ))
                .toList();

        return new MedicationDtoResponse(
                medication.getId(),
                medication.getPrescription(),
                meds
        );
    }
}
