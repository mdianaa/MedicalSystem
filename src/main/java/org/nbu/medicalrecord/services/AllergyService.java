package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.AllergyDtoRequest;
import org.nbu.medicalrecord.dtos.response.AllergyDtoResponse;

import java.util.Set;

public interface AllergyService {

    AllergyDtoResponse addAllergy(AllergyDtoRequest  allergyDtoRequest);

    AllergyDtoResponse showAllergy(String allergen);

    Set<AllergyDtoResponse> listAllergies();

    void deleteAllergy(String allergen);
}
