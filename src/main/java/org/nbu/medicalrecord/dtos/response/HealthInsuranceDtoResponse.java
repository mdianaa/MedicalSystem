package org.nbu.medicalrecord.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Month;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HealthInsuranceDtoResponse {

    private long id;

    private long patientId;

    private Month month;

    private int year;

    private boolean paid;
}
