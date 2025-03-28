package org.nbu.medicalrecord.dtos.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DoctorsScheduleDtoResponse {

    @NotBlank
    private String doctorFirstName;

    @NotBlank
    private String doctorLastName;

    @NotBlank
    private String shiftType;

    private Set<DoctorAppointmentDtoResponse> appointmentDtoResponses;
}
