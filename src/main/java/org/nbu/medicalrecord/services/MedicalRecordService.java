package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.MedicalRecordDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicalRecordDtoResponse;

import java.util.Set;

public interface MedicalRecordService {

    MedicalRecordDtoResponse createNewMedicalRecord(MedicalRecordDtoRequest req);

    MedicalRecordDtoResponse showMedicalRecord(long patientId);

    Set<MedicalRecordDtoResponse> showAllMedicalRecords();

    void deleteMedicalRecord(long medicalRecordId);
}
