package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(max = 10000)
    private String complaints;

    @Size(max = 10000)
    private String medicalHistory;

    @NotNull
    @Size(max = 10000)
    private Long diagnosisId;

    private Long medicationId;

    @Size(max = 10000)
    private String requiredTests;

    private Long sickLeaveId;

    @NotNull
    private Long medicalRecordId;

}
