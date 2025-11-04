package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminCreatePatientRequest {

    @NotBlank
    @Size(min=10,max=10)
    @Pattern(regexp="\\d{10}")
    private String egn;

    @NotBlank
    @Size(max=30)
    private String firstName;

    @NotBlank
    @Size(max=30)
    private String lastName;

    @NotNull
    private LocalDate birthDate;

    @Email
    private String email;

    @Size(min=8, max=100)
    @NotBlank
    private String password;
}
