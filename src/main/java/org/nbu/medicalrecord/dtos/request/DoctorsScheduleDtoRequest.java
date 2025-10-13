package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Long doctorId;

    @NotNull
    private ShiftType shift;

}
