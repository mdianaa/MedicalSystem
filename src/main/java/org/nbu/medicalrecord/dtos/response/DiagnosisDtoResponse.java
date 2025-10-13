package org.nbu.medicalrecord.dtos.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DiagnosisDtoResponse {

    private Long id;

    private String doctorName;

    private String patientName;

    private String complaints;

    private String medicalHistory;

    private List<String> allergies;

    private String diagnosisResult;

    private MedicationDtoResponse medication;

    private String requiredTests;

    private LocalDateTime createdAt;

}
