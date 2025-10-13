package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.nbu.medicalrecord.entities.Specialization;

@Data
public class AdminCreateDoctorRequest {

    @NotNull
    @Email
    private String email;

    @NotBlank
    @Size(min=10,max=10)
    @Pattern(regexp="\\d{10}")
    private String egn;

    @NotNull
    @Size(max=30)
    private String firstName;

    @NotNull
    @Size(max=30)
    private String lastName;

    @NotNull
    private Specialization specialization;
}
