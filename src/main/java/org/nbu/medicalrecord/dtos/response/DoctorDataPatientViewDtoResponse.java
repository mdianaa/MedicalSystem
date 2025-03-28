package org.nbu.medicalrecord.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DoctorDataPatientViewDtoResponse {

    private String firstName;

    private String lastName;

    private String specializationType;

    private String gp;
}
