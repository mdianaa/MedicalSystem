package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VisitDtoRequest {

    @NotNull
    private Long appointmentId;

    private Long diagnosisId;

    private Long sickLeaveId;

}
