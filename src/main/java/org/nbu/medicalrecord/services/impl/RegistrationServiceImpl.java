package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.AdminCreateDoctorRequest;
import org.nbu.medicalrecord.dtos.request.AdminCreatePatientRequest;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.entities.Specialization;
import org.nbu.medicalrecord.entities.User;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.repositories.SpecializationRepository;
import org.nbu.medicalrecord.repositories.UserRepository;
import org.nbu.medicalrecord.services.RegistrationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserRepository users;
    private final PatientRepository patients;
    private final DoctorRepository doctors;
    private final SpecializationRepository specializations;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createPatient(AdminCreatePatientRequest req) {
        String egn = req.getEgn();
        String email = normalize(req.getEmail());

        if (users.existsByEmail(email)) throw new IllegalStateException("Email " + req.getEmail() + " already registered");
        if (patients.existsByUser_Egn(egn)) throw new IllegalStateException("EGN " + req.getEgn() + " already exists");

        var user = new User();
        user.setEgn(egn);
        user.setEmail(email);
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEnabled(true);
        user.setLocked(false);
        user.setAuthorities(Set.of("PATIENT"));

        var p = new Patient();
        p.setBirthDate(req.getBirthDate());
        p.setUser(user);

        users.save(user);
        patients.save(p);
    }

    @Transactional
    public void createDoctor(AdminCreateDoctorRequest req) {
        String egn = req.getEgn();
        String email = normalize(req.getEmail());

        if (users.existsByEmail(email)) throw new IllegalStateException("Email " + req.getEmail() + " already registered");
        if (doctors.existsByUser_Egn(egn)) throw new IllegalStateException("EGN " + req.getEgn() + " already exists");

        var user = new User();
        user.setEgn(egn);
        user.setEmail(email);
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEnabled(true);
        user.setLocked(false);
        user.setAuthorities(Set.of("DOCTOR"));

        var d = new Doctor();

        Specialization specialization = specializations.findById(req.getSpecializationId())
                .orElseThrow(()-> new IllegalStateException("Specialization not found with id: " + req.getSpecializationId()));

        if (specialization.getType().equals("GP")) {
            d.setGp(true);
        }

        d.setSpecialization(specialization);
        d.setUser(user);

        users.save(user);
        doctors.save(d);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}