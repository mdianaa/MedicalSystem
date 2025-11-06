package org.nbu.medicalrecord.services.impl;

import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.AllergyDtoRequest;
import org.nbu.medicalrecord.dtos.response.AllergyDtoResponse;
import org.nbu.medicalrecord.entities.Allergy;
import org.nbu.medicalrecord.repositories.AllergyRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.AllergyService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllergyServiceImpl implements AllergyService {

    private final AllergyRepository allergyRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public AllergyDtoResponse addAllergy(AllergyDtoRequest req) {
        String name = normalize(req.getAllergen());
        if (allergyRepository.existsByAllergenIgnoreCase(name)) {
            throw new IllegalStateException("Allergy already exists: " + name);
        }

        Allergy a = new Allergy();
        a.setAllergen(name);

        allergyRepository.save(a);
        return toDto(a);
    }

    @Override
    public AllergyDtoResponse showAllergy(String allergen) {
         Allergy a = allergyRepository.findByAllergenIgnoreCase(normalize(allergen))
                .orElseThrow(() -> new IllegalArgumentException("Allergy not found: " + allergen));
        return toDto(a);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<AllergyDtoResponse> listAllergies() {

        return allergyRepository.findAll(Sort.by(Sort.Direction.ASC, "allergen"))
                .stream()
                .map(AllergyServiceImpl::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public void deleteAllergy(String allergen) {
        Allergy a = allergyRepository.findByAllergenIgnoreCase(normalize(allergen))
                .orElseThrow(() -> new IllegalArgumentException("Allergy not found: " + allergen));

        if (patientRepository.existsByAllergies_Id(a.getId())) {
            throw new IllegalStateException(
                    "Cannot delete allergy '" + a.getAllergen() + "' because it is used by patient(s)."
            );
        }

        allergyRepository.delete(a);
    }

    private static String normalize(String s) {
        return s == null ? null : s.trim().toLowerCase(Locale.ROOT);
    }

    private static AllergyDtoResponse toDto(Allergy a) {
        return new AllergyDtoResponse(a.getId(), a.getAllergen());
    }
}
