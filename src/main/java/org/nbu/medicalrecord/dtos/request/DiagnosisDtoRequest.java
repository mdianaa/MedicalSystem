package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank
    private String complaints;

    @NotBlank
    private String medicalHistory;

    //TODO: how to add allergies to the diagnosis
    private Set<AllergyDtoRequest> allergyDtoRequests;

    @NotBlank
    private String diagnosisResult;

    //TODO: how to add medication to the diagnosis
    private Set<MedicationDtoRequest> medicationDtoRequests;

    private String requiredTests;
}
