package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.DiagnosisDtoRequest;
import org.nbu.medicalrecord.dtos.response.DiagnosisDtoResponse;

import java.util.Set;

public interface DiagnosisService {

    DiagnosisDtoResponse createDiagnosis(DiagnosisDtoRequest request);

    Set<DiagnosisDtoResponse> showAllDiagnosisForPatientId(Long patientId);

    Set<DiagnosisDtoResponse> showAllDiagnosisByDoctorId(Long doctorId);

    Set<DiagnosisDtoResponse> showMostFrequentDiagnosisResult();

    void deleteDiagnosis(Long diagnosisId);
}
