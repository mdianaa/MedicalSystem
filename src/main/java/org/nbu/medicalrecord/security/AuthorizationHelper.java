package org.nbu.medicalrecord.security;

import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.repositories.AppointmentRepository;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("authz") // name used inside @PreAuthorize
@RequiredArgsConstructor
public class AuthorizationHelper {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;


    public boolean isDoctor(Authentication authentication, Long doctorId) {
        String email = principalEmail(authentication);
        if (!StringUtils.hasText(email) || doctorId == null) return false;

        Doctor doctor = doctorRepository.findByUser_Email(email);
        return doctor != null && doctorId.equals(doctor.getId());
    }

    public boolean isPatient(Authentication authentication, Long patientId) {
        String email = principalEmail(authentication);
        if (!StringUtils.hasText(email) || patientId == null) return false;

        Patient patient = patientRepository.findByUser_Email(email);
        return patient != null && patientId.equals(patient.getId());
    }


    /** Is the authenticated doctor the GP of this patient? */
    public boolean isDoctorOfPatient(Authentication authentication, Long patientId) {
        String email = principalEmail(authentication);
        if (!StringUtils.hasText(email) || patientId == null) return false;

        Doctor doctor = doctorRepository.findByUser_Email(email);
        if (doctor == null) return false;

        return patientRepository.existsByIdAndGp_Id(patientId, doctor.getId());
    }

    /** Did the authenticated doctor create/own this appointment? */
    public boolean isDoctorOfAppointment(Authentication authentication, Long appointmentId) {
        String email = principalEmail(authentication);
        if (!StringUtils.hasText(email) || appointmentId == null) return false;

        Doctor doctor = doctorRepository.findByUser_Email(email);
        if (doctor == null) return false;

        return appointmentRepository.existsByIdAndDoctor_Id(appointmentId, doctor.getId());
    }

    private String principalEmail(Authentication authentication) {
        return (authentication == null) ? null : authentication.getName();
    }
}
