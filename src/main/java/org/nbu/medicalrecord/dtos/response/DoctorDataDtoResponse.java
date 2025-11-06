package org.nbu.medicalrecord.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DoctorDataDtoResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private SpecializationDtoResponse specialization;

    private boolean gp;

    private int gpPatientsCount;
}
