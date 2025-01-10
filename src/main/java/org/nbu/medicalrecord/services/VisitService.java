package org.nbu.medicalrecord.services;

public interface VisitService {
    void createNewVisit();
    void showAllVisitsByDoctor();
    void showAllVisitsForPeriod();
    void showAllVisitsByDoctorForPeriod();
    void showAllVisitsForPatient();
}
