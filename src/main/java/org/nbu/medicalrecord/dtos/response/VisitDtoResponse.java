package org.nbu.medicalrecord.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VisitDtoResponse {

    private Long id;

    private String doctorName;

    private Long appointmentId;

    private Long medicalRecordId;

    private Long sickLeaveId;

    private Long medicationId;

    private Long diagnosisId;

    private String complaints;

    private String medicalHistory;

    private String requiredTests;

}
