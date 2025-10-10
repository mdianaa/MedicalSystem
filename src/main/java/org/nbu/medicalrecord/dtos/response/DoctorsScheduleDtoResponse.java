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

    private long scheduleId;

    private long doctorId;

    private ShiftType shift;

    int appointmentsCount;
}
