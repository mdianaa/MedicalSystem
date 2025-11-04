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
public class DoctorAppointmentDtoResponse {

    private Long id;

    private Long patientId;

    private String patientName;

    private LocalDate date;

    private LocalTime hourOfAppointment;

}
