package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.AdminCreateDoctorRequest;
import org.nbu.medicalrecord.dtos.request.AdminCreatePatientRequest;

public interface RegistrationService {

    void createPatient(AdminCreatePatientRequest req);

    void createDoctor(AdminCreateDoctorRequest req);
}
