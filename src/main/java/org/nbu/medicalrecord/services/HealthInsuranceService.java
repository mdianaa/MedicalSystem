package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.HealthInsuranceDtoRequest;
import org.nbu.medicalrecord.dtos.response.HealthInsuranceDtoResponse;

import java.time.Month;
import java.time.Year;
import java.util.Set;

public interface HealthInsuranceService {

    HealthInsuranceDtoResponse createNewHealthInsurance(HealthInsuranceDtoRequest healthInsuranceDtoRequest);

    void payHealthInsuranceForMonthInYear(long patientId, Month month, Year year);

    void payHealthInsuranceForMonthsInYear(long patientId, Set<Month> months, Year year);

    Set<HealthInsuranceDtoResponse> referenceForLastSixMonths();
}
