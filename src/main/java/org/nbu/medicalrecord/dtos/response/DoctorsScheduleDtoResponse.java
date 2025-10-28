package org.nbu.medicalrecord.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nbu.medicalrecord.enums.ShiftType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DoctorsScheduleDtoResponse {

    private Long scheduleId;

    private Long doctorId;

    private ShiftType shift;
}
