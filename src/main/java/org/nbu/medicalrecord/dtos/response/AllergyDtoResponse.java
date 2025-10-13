package org.nbu.medicalrecord.dtos.response;

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
public class AllergyDtoResponse {

    private Long id;

    private String allergen;

    private AllergyType allergyType;

    private Long diagnosesCount;
}
