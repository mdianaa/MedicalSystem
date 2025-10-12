package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.response.PatientDataWithDoctorDtoResponse;

import java.util.Set;

public interface PatientService {

    Set<PatientDataWithDoctorDtoResponse> showAllPatientsWithGP(long doctorId);

    int totalCountPatientsWithGP(long doctorId);

    Set<PatientDataWithDoctorDtoResponse> showAllPatients();

    Set<PatientDataWithDoctorDtoResponse> showAllPatientsWhoVisitedDoctor(long doctorId);

    int totalCountPatientsWhoVisitedDoctor(long doctorId);

    Set<PatientDataWithDoctorDtoResponse> showAllPatientsWithResultDiagnosis(String result);

    int totalCountPatientsWithResultDiagnosis(String result);

    Set<PatientDataWithDoctorDtoResponse> showAllPatientsWithAllergy(String allergen);
}
