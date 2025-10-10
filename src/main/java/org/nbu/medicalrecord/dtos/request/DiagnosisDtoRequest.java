package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DiagnosisDtoRequest {

    @NotNull
    private long doctorId;

    @NotNull
    private long patientId;

    @NotBlank
    private String complaints;

    private String medicalHistory;

    @NotBlank
    private String diagnosisResult;

    private MedicationDtoRequest medication; // nullable if no meds

    private String requiredTests;

}
