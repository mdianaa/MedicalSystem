package org.nbu.medicalrecord.dtos.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    private long id;

    private long patientId;

    private String patientName;

    private LocalDate date;

    private LocalTime hourOfAppointment;

}
