package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.AllergyDtoRequest;
import org.nbu.medicalrecord.dtos.response.AllergyDtoResponse;
import org.nbu.medicalrecord.entities.Allergy;
import org.nbu.medicalrecord.repositories.AllergyRepository;
import org.nbu.medicalrecord.services.AllergyService;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AllergyServiceImpl implements AllergyService {

    private final AllergyRepository allergyRepository;

    @Override
    @Transactional
    public AllergyDtoResponse addAllergy(AllergyDtoRequest req) {
        String name = normalize(req.getAllergen());
        if (allergyRepository.existsByAllergenIgnoreCase(name)) {
            throw new IllegalStateException("Allergy already exists: " + name);
        }

        Allergy a = new Allergy();
        a.setAllergen(name);
        a.setAllergyType(req.getAllergyType());
        a.setDiagnoses(null); // leave null or empty; JPA will manage the owning side on Diagnosis

        allergyRepository.save(a);
        return toDto(a, 0);
    }

    @Override
    public AllergyDtoResponse showAllergy(String allergen) {
        var a = allergyRepository.findWithDiagnosesByAllergenIgnoreCase(normalize(allergen))
                .orElseThrow(() -> new IllegalArgumentException("Allergy not found: " + allergen));
        long count = (a.getDiagnoses() == null) ? 0 : a.getDiagnoses().size();
        return toDto(a, count);
    }

    @Override
    @Transactional
    public void deleteAllergy(String allergen) {
        var a = allergyRepository.findWithDiagnosesByAllergenIgnoreCase(normalize(allergen))
                .orElseThrow(() -> new IllegalArgumentException("Allergy not found: " + allergen));

        // Safety: prevent deleting vocab terms still referenced by diagnoses
        if (a.getDiagnoses() != null && !a.getDiagnoses().isEmpty()) {
            throw new IllegalStateException("Cannot delete allergy; it is referenced by diagnoses.");
        }
        allergyRepository.delete(a);
    }

    private static String normalize(String s) {
        return s == null ? null : s.trim().toLowerCase(Locale.ROOT);
    }

    private static AllergyDtoResponse toDto(Allergy a, long diagnosesCount) {
        return new AllergyDtoResponse(a.getId(), a.getAllergen(), a.getAllergyType(), diagnosesCount);
    }
}
