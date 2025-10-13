package org.nbu.medicalrecord.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VisitDtoResponse {

    private long id;

    private long appointmentId;

    private long doctorId;

    private String doctorName;

    private long patientId;

    private String patientName;

    private long medicalRecordId;

    private LocalDate date;

    private LocalTime hour;

    private long diagnosisId;

    private long sickLeaveId;

}
