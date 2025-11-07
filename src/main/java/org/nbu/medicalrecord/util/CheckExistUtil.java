package org.nbu.medicalrecord.util;

import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;

public final class CheckExistUtil {

    public static void checkIfPatientExists(PatientRepository patientRepository, Long patientId) {
        if (patientRepository.findById(patientId).isEmpty()) {
            throw new IllegalArgumentException("Patient with id " + patientId + " not found");
        }
    }

    public static void checkIfDoctorExists(DoctorRepository doctorRepository, Long doctorId) {
        if (doctorRepository.findById(doctorId).isEmpty()) {
            throw new IllegalArgumentException("Doctor with id " + doctorId + " not found");
        }
    }
}
