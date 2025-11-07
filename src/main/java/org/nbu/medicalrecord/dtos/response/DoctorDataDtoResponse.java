package org.nbu.medicalrecord.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DoctorDataDtoResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private Set<SpecializationDtoResponse> specializations;

    private boolean gp;

    private int gpPatientsCount;
}
