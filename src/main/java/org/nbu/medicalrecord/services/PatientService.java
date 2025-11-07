package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.response.PatientDataWithDoctorDtoResponse;

import java.util.Set;

public interface PatientService {

    Set<PatientDataWithDoctorDtoResponse> showAllPatientsWithGP(long doctorId);

    Set<PatientDataWithDoctorDtoResponse> showAllPatients();

    Set<PatientDataWithDoctorDtoResponse> showAllPatientsWhoVisitedDoctor(long doctorId);

    int totalCountPatientsWhoVisitedDoctor(long doctorId);

    Set<PatientDataWithDoctorDtoResponse> showAllPatientsWithSameDiagnosis(String diagnosis);

    int totalCountPatientsWithSameDiagnosis(String diagnosis);

    Set<PatientDataWithDoctorDtoResponse> showAllPatientsWithAllergy(String allergen);
}
