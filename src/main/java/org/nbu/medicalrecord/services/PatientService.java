package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.response.PatientDataDtoResponse;

import java.util.Set;

public interface PatientService {

    Set<PatientDataDtoResponse> showAllPatientsWithGP(long doctorId);

    int totalCountPatientsWithGP(long doctorId);

    Set<PatientDataDtoResponse> showAllPatients();

    Set<PatientDataDtoResponse> showAllPatientsWhoVisitedDoctor(long doctorId);

    int totalCountPatientsWhoVisitedDoctor(long doctorId);

    Set<PatientDataDtoResponse> showAllPatientsWithResultDiagnosis(String result);

    int totalCountPatientsWithResultDiagnosis(String result);

    Set<PatientDataDtoResponse> showAllPatientsWithAllergy(String allergen);
}
