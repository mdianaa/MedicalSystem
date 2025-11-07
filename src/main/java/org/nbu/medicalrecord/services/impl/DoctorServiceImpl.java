package org.nbu.medicalrecord.services.impl;

import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.response.DoctorDataDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientDataDtoResponse;
import org.nbu.medicalrecord.dtos.response.SpecializationDtoResponse;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.repositories.SpecializationRepository;
import org.nbu.medicalrecord.services.DoctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SpecializationRepository specializationRepository;

    @Override
    @Transactional
    public PatientDataDtoResponse addNewPatientForGpById(Long patientId, Long doctorId) {
        Doctor gp = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor with id " + doctorId + " not found"));

        if (!gp.isGp()) {
            throw new IllegalStateException("Doctor is not a GP.");
        }

        Patient p = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient with id " + patientId + " not found"));

        p.setGp(gp);
        Patient saved = patientRepository.save(p);
        return toPatientDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public int countTotalPatientsForGpById(Long doctorId) {
        Doctor gp = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor with id " + doctorId + " not found"));
        return gp.getGpPatients() == null ? 0 : gp.getGpPatients().size();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<DoctorDataDtoResponse> showAllGPs() {
        return doctorRepository.findByGpTrue().stream()
                .map(this::toDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<DoctorDataDtoResponse> showAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::toDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<DoctorDataDtoResponse> showAllDoctorsWithSpecialization(String specializationType) {
         if (!specializationRepository.existsByTypeIgnoreCase(specializationType)) {
             throw new IllegalArgumentException("Specialization '" + specializationType + "' not found");
         }
        return doctorRepository.findBySpecializationType(specializationType).stream()
                .map(this::toDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<DoctorDataDtoResponse> showAllDoctorsWithMostSickLeavesGiven() {
        List<DoctorRepository.DoctorSickLeaveCount> counts = doctorRepository.countSickLeavesPerDoctor();

        List<Long> ids = counts.stream().map(DoctorRepository.DoctorSickLeaveCount::getDoctorId).toList();
        Map<Long, Doctor> byId = ids.isEmpty()
                ? Collections.emptyMap()
                : doctorRepository.findAllById(ids).stream().collect(Collectors.toMap(Doctor::getId, d -> d));

        return counts.stream()
                .map(c -> byId.get(c.getDoctorId()))
                .filter(Objects::nonNull)
                .map(this::toDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // --- Mappers ---

    private DoctorDataDtoResponse toDoctorDto(Doctor d) {
        Set<SpecializationDtoResponse> specDtos =
                (d.getSpecializations() == null)
                        ? Set.of()
                        : d.getSpecializations().stream()
                        .map(s -> new SpecializationDtoResponse(s.getId(), s.getType()))
                        .collect(Collectors.toSet());

        int count = (d.getGpPatients() == null) ? 0 : d.getGpPatients().size();

        return new DoctorDataDtoResponse(
                d.getId(),
                d.getUser().getFirstName(),
                d.getUser().getLastName(),
                specDtos,
                d.isGp(),
                count
        );
    }

    private PatientDataDtoResponse toPatientDto(Patient p) {
        return new PatientDataDtoResponse(
                p.getId(),
                p.getUser().getFirstName(),
                p.getUser().getLastName(),
                p.getBirthDate()
        );
    }
}
