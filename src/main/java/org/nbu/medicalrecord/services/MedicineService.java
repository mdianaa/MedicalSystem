package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.MedicineDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicineDtoResponse;

import java.util.Set;

public interface MedicineService {

    MedicineDtoResponse addMedicine(MedicineDtoRequest medicineDtoRequest);

    MedicineDtoResponse showMedicine(String name, int mg);

    Set<MedicineDtoResponse> showAllMedicines();

//    void editMedicine();

    void deleteMedicine(long medicineId);
}
