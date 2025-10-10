package org.nbu.medicalrecord.dtos.response;

import jakarta.validation.constraints.NotBlank;
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
public class PatientAppointmentDtoResponse {

    private long id;

    private long patientId;

    private long doctorId;

    private String doctorName;

    private LocalDate date;

    private LocalTime hourOfAppointment;
}
