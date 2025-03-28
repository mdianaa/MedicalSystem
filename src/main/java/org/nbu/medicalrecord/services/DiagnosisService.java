package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.DiagnosisDtoRequest;
import org.nbu.medicalrecord.dtos.response.DiagnosisDtoResponse;

import java.util.Set;

public interface DiagnosisService {

    DiagnosisDtoResponse createDiagnosis(DiagnosisDtoRequest diagnosisDtoRequest);

    Set<DiagnosisDtoResponse> showAllDiagnosisForPatient(int patientEgn);

    Set<DiagnosisDtoResponse> showAllDiagnosisByDoctor(int doctorEgn);

    Set<DiagnosisDtoResponse> showMostFrequentDiagnosisResult();

//    void editDiagnosis();

    void deleteDiagnosis();
}
