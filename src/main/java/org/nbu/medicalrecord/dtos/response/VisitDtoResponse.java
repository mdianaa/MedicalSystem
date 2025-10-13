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

    private Long id;

    private Long appointmentId;

    private Long doctorId;

    private String doctorName;

    private Long patientId;

    private String patientName;

    private Long medicalRecordId;

    private LocalDate date;

    private LocalTime hour;

    private Long diagnosisId;

    private Long sickLeaveId;

}
