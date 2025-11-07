package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.SpecializationDtoRequest;
import org.nbu.medicalrecord.dtos.response.SpecializationDtoResponse;
import org.nbu.medicalrecord.entities.Specialization;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.SpecializationRepository;
import org.nbu.medicalrecord.services.SpecializationService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecializationServiceImpl implements SpecializationService {

    private final SpecializationRepository specializationRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional
    public SpecializationDtoResponse addNewSpecialization(SpecializationDtoRequest req) {
        String name = normalize(req.getType());
        if (specializationRepository.existsByTypeIgnoreCase(name)) {
            throw new IllegalStateException("Specialization " + name + " already exists");
        }

        Specialization s = new Specialization();
        s.setType(name);

        specializationRepository.save(s);

        return toDto(s);
    }

    @Override
    public Set<SpecializationDtoResponse> showAllSpecializations() {
        return specializationRepository.findAllByOrderByTypeAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public void deleteSpecialization(long specializationId) {
        if (!specializationRepository.existsById(specializationId)) {
            throw new IllegalArgumentException("Specialization not found");
        }

        if (doctorRepository.existsBySpecializations_Id(specializationId)) {
            throw new IllegalStateException("Cannot delete specialization: it is used by at least one doctor.");
        }

        specializationRepository.deleteById(specializationId);
    }

    private SpecializationDtoResponse toDto(Specialization s) {
        return new SpecializationDtoResponse(s.getId(), s.getType());
    }

    private static String normalize(String s) {
        return s == null ? null : s.trim();
    }
}
