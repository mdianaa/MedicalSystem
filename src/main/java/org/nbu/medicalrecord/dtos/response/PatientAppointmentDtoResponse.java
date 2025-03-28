package org.nbu.medicalrecord.dtos.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PatientAppointmentDtoResponse {

    private String date;

    private String hourOfAppointment;

    private String doctorFirstName;

    private String doctorLastName;
}
