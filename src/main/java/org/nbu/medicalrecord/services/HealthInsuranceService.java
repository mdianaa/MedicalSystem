package org.nbu.medicalrecord.services;

public interface HealthInsuranceService {
    void createNewHealthInsurance();
    void payHealthInsuranceForMonth();
    void payHealthInsuranceForMonths();
    void referenceForLastSixMonths();
}
