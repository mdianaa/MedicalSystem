package org.nbu.medicalrecord.services;

public interface PatientService {
    void showAllPatientsWithGP();
    int totalCountPatientsWithGP();
    void showAllPatients();
    void showAllPatientsWhoVisitedDoctor();
    int totalCountPatientsWhoVisitedDoctor();
    void showAllPatientsWithResultDiagnosis();
    int totalCountPatientsWithResultDiagnosis();
    void showAllPatientsWithAllergy();
}
