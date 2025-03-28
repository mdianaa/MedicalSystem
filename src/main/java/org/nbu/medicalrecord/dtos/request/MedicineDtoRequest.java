package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.nbu.medicalrecord.enums.MedicineType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MedicineDtoRequest {

    @NotBlank
    @Length(max = 30)
    private String name;

    @NotNull
    @Positive
    @Min(1)
    private int mg;

    @NotNull
    private MedicineType medicineType;
}
