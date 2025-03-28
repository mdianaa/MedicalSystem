package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nbu.medicalrecord.enums.AllergyType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AllergyDtoRequest {

    @NotBlank
    private String allergen;

    @NotBlank
    private AllergyType allergyType;
}
