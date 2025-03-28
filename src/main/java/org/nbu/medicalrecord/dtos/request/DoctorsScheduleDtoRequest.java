package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nbu.medicalrecord.enums.ShiftType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DoctorsScheduleDtoRequest {

    @NotBlank
    @Size(min = 10, max = 10, message = "Personal ID must be exactly 10 digits.")
    @Pattern(regexp = "\\d{10}", message = "Personal ID must contain only digits.")
    private int doctorEgn;

    @NotNull
    private ShiftType shiftType;

}
