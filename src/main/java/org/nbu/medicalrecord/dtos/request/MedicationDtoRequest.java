package org.nbu.medicalrecord.dtos.request;

import jakarta.persistence.Lob;
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
public class MedicationDtoRequest {

    private Set<MedicineDtoRequest> medicines; //TODO: how to reach the medicines

    @NotBlank
    @Lob
    private String prescription;
}
