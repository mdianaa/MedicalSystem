package org.nbu.medicalrecord.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PatientDataDtoResponse {

    private String egn;

    private String firstName;

    private String lastName;

    private LocalDate birthDate;

    private String gpLastName;

}
