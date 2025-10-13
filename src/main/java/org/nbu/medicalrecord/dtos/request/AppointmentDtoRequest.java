package org.nbu.medicalrecord.dtos.request;

import jakarta.validation.constraints.NotNull;
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
public class AppointmentDtoRequest {

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime hourOfAppointment;

    @NotNull
    private Long doctorId;

    @NotNull
    private Long patientId;

}
