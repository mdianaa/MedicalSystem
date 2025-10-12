package org.nbu.medicalrecord.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PatientDataWithDoctorDtoResponse {

    private PatientDataDtoResponse patientData;

    private DoctorDataPatientViewDtoResponse doctorData;

}
