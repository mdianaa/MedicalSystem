package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.response.DoctorDataDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientDataDtoResponse;
import org.nbu.medicalrecord.dtos.response.SpecializationDtoResponse;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.entities.Specialization;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.DoctorService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public PatientDataDtoResponse addNewPatientForGpById(Long patientId, Long doctorId) {
        Doctor gp = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        if (!gp.isGp()) {
            throw new IllegalStateException("Doctor is not a GP.");
        }

        Patient p = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        p.setGp(gp);
        Patient saved = patientRepository.save(p);
        return toPatientDto(saved);
    }

    @Override
    public int countTotalPatientsForGpById(Long doctorId) {
        Doctor gp = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        return gp.getGpPatients() == null ? 0 : gp.getGpPatients().size();
    }

    @Override
    public Set<DoctorDataDtoResponse> showAllGPs() {
        return doctorRepository.findByGpTrue().stream()
                .map(this::toDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<DoctorDataDtoResponse> showAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::toDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<DoctorDataDtoResponse> showAllDoctorsWithSpecialization(String specializationType) {
        return doctorRepository.findBySpecialization_TypeIgnoreCase(specializationType).stream()
                .map(this::toDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<DoctorDataDtoResponse> showAllDoctorsWithMostSickLeavesGiven() {
        List<DoctorRepository.DoctorSickLeaveCount> counts = doctorRepository.countSickLeavesPerDoctor();
        Map<Long, Doctor> byId = doctorRepository.findAll().stream()
                .collect(Collectors.toMap(Doctor::getId, d -> d));

        return counts.stream()
                .map(c -> byId.get(c.getDoctorId()))
                .filter(Objects::nonNull)
                .map(this::toDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private DoctorDataDtoResponse toDoctorDto(Doctor d) {
        Specialization spec = d.getSpecialization();
        SpecializationDtoResponse specDto = (spec == null)
                ? null
                : new SpecializationDtoResponse(spec.getId(), spec.getType());

        int count = (d.getGpPatients() == null) ? 0 : d.getGpPatients().size();

        return new DoctorDataDtoResponse(
                d.getId(),
                d.getUser() != null ? d.getUser().getId() : null,
                specDto,
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
