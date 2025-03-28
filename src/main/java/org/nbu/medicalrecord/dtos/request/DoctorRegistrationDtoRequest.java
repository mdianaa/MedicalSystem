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
import org.nbu.medicalrecord.entities.Specialization;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DoctorRegistrationDtoRequest {

    @NotBlank
    @Length(max = 30)
    private String firstName;

    @NotBlank
    @Length(max = 30)
    private String lastName;

    @Column(name = "egn", unique = true, length = 10)
    @NotBlank
    @Size(min = 10, max = 10, message = "Personal ID must be exactly 10 digits.")
    @Pattern(regexp = "\\d{10}", message = "Personal ID must contain only digits.")
    private int egn;

    // TODO: password?

    @NotNull
    private Specialization specialization;

    @NotNull
    private boolean gp;
}
