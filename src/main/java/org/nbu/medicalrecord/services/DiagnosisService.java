package org.nbu.medicalrecord.services;

public interface DiagnosisService {
    void createDiagnosis();
    void showAllDiagnosisForPatient();
    void showAllDiagnosisByDoctor();
    void showMostFrequentDiagnosisResult();
    void editDiagnosis();
    void deleteDiagnosis();
}
