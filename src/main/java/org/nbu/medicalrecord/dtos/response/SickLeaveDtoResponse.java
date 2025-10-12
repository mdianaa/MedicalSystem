package org.nbu.medicalrecord.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SickLeaveDtoResponse {

    private Long id;

    private long doctorId;

    private String doctorName;

    private long patientId;

    private String patientName;

    private LocalDate fromDate;

    private LocalDate toDate;

    private String reason;

    private int totalDays;

}
