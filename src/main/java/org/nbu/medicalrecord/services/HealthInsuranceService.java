package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.HealthInsuranceDtoRequest;
import org.nbu.medicalrecord.dtos.response.HealthInsuranceDtoResponse;

import java.time.Month;
import java.util.Set;

public interface HealthInsuranceService {

    HealthInsuranceDtoResponse createNewHealthInsurance(HealthInsuranceDtoRequest req);

    void createMonthlyHealthInsuranceRows();

    void payHealthInsuranceForMonthInYear(long patientId, Month month, int year);

    void payHealthInsuranceForMonthsInYear(long patientId, Set<Month> months, int year);

    Set<HealthInsuranceDtoResponse> referenceForLastSixMonthsByPatientId(Long patientId);

    Set<HealthInsuranceDtoResponse> findAllByPatient(Long patientId);

    Set<HealthInsuranceDtoResponse> findAllPaidByPatient(Long patientId);

    Set<HealthInsuranceDtoResponse> findAllUnpaidByPatient(Long patientId);
}