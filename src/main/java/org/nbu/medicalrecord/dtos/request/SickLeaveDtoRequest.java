package org.nbu.medicalrecord.dtos.request;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SickLeaveDtoRequest {

    @NotNull
    private LocalDate fromDate;

    @NotNull
    private LocalDate toDate;

    @NotBlank
    @Lob
    private String reason;

    @NotBlank
    @Size(min = 10, max = 10, message = "Personal ID must be exactly 10 digits.")
    @Pattern(regexp = "\\d{10}", message = "Personal ID must contain only digits.")
    private int patientEgn;

}
