package org.nbu.medicalrecord.dtos.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PatientRegisterDtoRequest {

    @NotBlank
    @Size(min = 10, max = 10, message = "Personal ID must be exactly 10 digits.")
    @Pattern(regexp = "\\d{10}", message = "Personal ID must contain only digits.")
    private int egn;

    // TODO: password!!!
    private String password;

    @NotBlank
    @Length(max = 30)
    private String firstName;

    @NotBlank
    @Length(max = 30)
    private String lastName;

    @NotNull
    private LocalDate birthDate;

    @NotBlank
    @Size(min = 10, max = 10, message = "Personal ID must be exactly 10 digits.")
    @Pattern(regexp = "\\d{10}", message = "Personal ID must contain only digits.")
    private String gpEgn;

}
