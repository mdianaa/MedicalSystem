package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.AllergyDtoRequest;
import org.nbu.medicalrecord.dtos.response.AllergyDtoResponse;

public interface AllergyService {

    AllergyDtoResponse addAllergy(AllergyDtoRequest  allergyDtoRequest);

    AllergyDtoResponse showAllergy(String allergen);

    void deleteAllergy(String allergen);
}
