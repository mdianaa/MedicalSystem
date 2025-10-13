package org.nbu.medicalrecord.dtos.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MedicalRecordDtoResponse {

    private Long id;

    private Long patientId;

    private String firstName;

    private String lastName;

    private LocalDate birthDate;

    private int visitsCount;

}
