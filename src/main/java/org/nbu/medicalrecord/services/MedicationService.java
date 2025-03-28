package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.MedicationDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicationDtoResponse;

import java.util.Set;

public interface MedicationService {

    MedicationDtoResponse addMedication(MedicationDtoRequest medicationDtoRequest);

    Set<MedicationDtoResponse> showAllMedications();

    Set<MedicationDtoResponse> showAllMedicationsByDoctor(long doctorId);
}
