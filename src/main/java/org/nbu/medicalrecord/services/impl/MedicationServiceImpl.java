package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.MedicationDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicationDtoResponse;
import org.nbu.medicalrecord.dtos.response.MedicineDtoResponse;
import org.nbu.medicalrecord.entities.Medication;
import org.nbu.medicalrecord.entities.Medicine;
import org.nbu.medicalrecord.entities.Visit;
import org.nbu.medicalrecord.repositories.*;
import org.nbu.medicalrecord.services.MedicationService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.nbu.medicalrecord.util.CheckExistUtil.*;

@Service
@RequiredArgsConstructor
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;
    private final MedicineRepository medicineRepository;
    private final DoctorRepository doctorRepository;
    private final VisitRepository visitRepository;

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
    @Transactional
    public Set<MedicationDtoResponse> showAllMedicationsByDoctor(long doctorId) {
        checkIfDoctorExists(doctorRepository, doctorId);

        return visitRepository.findByAppointment_Doctor_IdOrderByAppointment_DateDesc(doctorId).stream()
                .map(Visit::getMedication)
                .filter(Objects::nonNull)
                .filter(m -> m.getId() != null)
                .collect(java.util.stream.Collectors.toMap(
                        Medication::getId,
                        m -> m,
                        (existing, ignored) -> existing,
                        java.util.LinkedHashMap::new
                ))
                .values().stream()
                .map(this::toDto)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
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