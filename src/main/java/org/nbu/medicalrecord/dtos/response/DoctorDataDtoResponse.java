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

    private Long userId;

    private SpecializationDtoResponse specialization;

    private boolean gp;

    private int gpPatientsCount;
}
