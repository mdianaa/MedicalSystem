package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.response.DoctorDataDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientDataDtoResponse;

import java.util.Set;

public interface DoctorService {

    PatientDataDtoResponse addNewPatientForGpById(Long patientId, Long doctorId);

    int countTotalPatientsForGpById(Long doctorId);

    Set<DoctorDataDtoResponse> showAllGPs();

    Set<DoctorDataDtoResponse> showAllDoctors();

    Set<DoctorDataDtoResponse> showAllDoctorsWithSpecialization(String specializationType);

    Set<DoctorDataDtoResponse> showAllDoctorsWithMostSickLeavesGiven();
}
