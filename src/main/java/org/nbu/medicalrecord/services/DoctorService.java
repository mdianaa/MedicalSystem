package org.nbu.medicalrecord.services;

public interface DoctorService {
    void addNewPatientForGp();
    void countTotalPatientsForGp();
    void showAllGPs();
    void showAllDoctors();
    void showAllDoctorsWithSpecialization();
    void showAllDoctorsWithMostSickLeavesGiven();
}
