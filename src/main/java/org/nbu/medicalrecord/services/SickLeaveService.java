package org.nbu.medicalrecord.services;

public interface SickLeaveService {
    void showAllSickLeavesByDoctor();
    void showAllSickLeavesForPatient();
    void showMonthWithMostSickLeaves();
}
