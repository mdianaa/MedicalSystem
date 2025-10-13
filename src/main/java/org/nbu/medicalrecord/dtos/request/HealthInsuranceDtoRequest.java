package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Month;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HealthInsuranceDtoRequest {

    @NotNull
    private Long patientId;

    @NotNull
    private Month month;

    @Min(1900)
    private int year;

    private boolean paid;
}
