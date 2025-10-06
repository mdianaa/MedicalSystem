package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRegistrationDtoRequest {

    // PATIENT identity (domain)
    @NotBlank
    @Size(min=10, max=10, message="Personal ID must be exactly 10 digits.")
    @Pattern(regexp="\\d{10}", message="Personal ID must contain only digits.")
    private String egn;

    @NotBlank
    @Size(max=30)
    private String firstName;

    @NotBlank
    @Size(max=30)
    private String lastName;

    @NotNull
    private LocalDate birthDate;

    // AUTH (user)
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min=8, max=72)
    private String password;
}
