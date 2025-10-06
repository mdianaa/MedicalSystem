package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminCreatePatientRequest {

    @NotBlank
    @Size(min=10,max=10)
    @Pattern(regexp="\\d{10}") String egn;

    @NotBlank
    @Size(max=30) String firstName;

    @NotBlank
    @Size(max=30) String lastName;

    @NotNull
    LocalDate birthDate;

    @Email
    String email;
}
