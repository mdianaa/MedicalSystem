package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.HealthInsuranceDtoRequest;
import org.nbu.medicalrecord.dtos.response.HealthInsuranceDtoResponse;

import java.time.Month;
import java.util.Set;

public interface HealthInsuranceService {

    HealthInsuranceDtoResponse createNewHealthInsurance(HealthInsuranceDtoRequest request);

    void payHealthInsuranceForMonthInYear(long patientId, Month month, int year);

    void payHealthInsuranceForMonthsInYear(long patientId, Set<Month> months, int year);

    Set<HealthInsuranceDtoResponse> referenceForLastSixMonthsByPatientId(Long patientId);
}