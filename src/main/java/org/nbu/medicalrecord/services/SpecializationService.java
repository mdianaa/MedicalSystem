package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.SpecializationDtoRequest;
import org.nbu.medicalrecord.dtos.response.SpecializationDtoResponse;

import java.util.Set;

public interface SpecializationService {

    SpecializationDtoResponse addNewSpecialization(SpecializationDtoRequest specializationDtoRequest);

    Set<SpecializationDtoResponse> showAllSpecializations();

    void deleteSpecialization(long specializationId);
}
