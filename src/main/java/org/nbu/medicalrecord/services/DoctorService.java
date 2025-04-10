package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.response.DoctorDataDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientDataDtoResponse;

import java.util.Set;

public interface DoctorService {

    PatientDataDtoResponse addNewPatientForGp(long patientEgn, long doctorEgn);

    int countTotalPatientsForGp(long doctorEgn);

    Set<DoctorDataDtoResponse> showAllGPs();

    Set<DoctorDataDtoResponse> showAllDoctors();

    Set<DoctorDataDtoResponse> showAllDoctorsWithSpecialization(String specializationType);

    Set<DoctorDataDtoResponse> showAllDoctorsWithMostSickLeavesGiven();
}
