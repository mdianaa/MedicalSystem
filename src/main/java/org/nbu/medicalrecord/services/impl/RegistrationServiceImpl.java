package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.AdminCreateDoctorRequest;
import org.nbu.medicalrecord.dtos.request.AdminCreatePatientRequest;
import org.nbu.medicalrecord.dtos.request.AllergyDtoRequest;
import org.nbu.medicalrecord.entities.*;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.repositories.SpecializationRepository;
import org.nbu.medicalrecord.repositories.UserRepository;
import org.nbu.medicalrecord.services.RegistrationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final SpecializationRepository specializationRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createPatient(AdminCreatePatientRequest req) {
        String egn = req.getEgn();
        String email = normalize(req.getEmail());

        if (userRepository.existsByEmail(email)) throw new IllegalStateException("Email " + req.getEmail() + " already registered");
        if (patientRepository.existsByUser_Egn(egn)) throw new IllegalStateException("EGN " + req.getEgn() + " already exists");

        User user = new User();
        user.setEgn(egn);
        user.setEmail(email);
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEnabled(true);
        user.setLocked(false);
        user.setAuthorities(Set.of("PATIENT"));

        Patient p = new Patient();
        p.setBirthDate(req.getBirthDate());

        Set<AllergyDtoRequest> allergyDtos =
                req.getAllergies() == null ? Set.of() : req.getAllergies();
        p.setAllergies(toAllergies(allergyDtos));

        p.setGp(null);
        p.setUser(user);

        userRepository.save(user);
        patientRepository.save(p);
    }

    @Transactional
    public void createDoctor(AdminCreateDoctorRequest req) {
        String egn = req.getEgn();
        String email = normalize(req.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email " + req.getEmail() + " already registered");
        }
        if (doctorRepository.existsByUser_Egn(egn)) {
            throw new IllegalStateException("EGN " + req.getEgn() + " already exists");
        }

        if (req.getSpecializationIds() == null || req.getSpecializationIds().isEmpty()) {
            throw new IllegalStateException("At least one specialization must be provided");
        }

        User user = new User();
        user.setEgn(egn);
        user.setEmail(email);
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEnabled(true);
        user.setLocked(false);
        user.setAuthorities(Set.of("DOCTOR"));

        HashSet<Long> requestedIds = new HashSet<>(req.getSpecializationIds());
        List<Specialization> foundSpecs = specializationRepository.findAllById(requestedIds);

        if (foundSpecs.size() != requestedIds.size()) {
            Set<Long> foundIds = foundSpecs.stream().map(Specialization::getId).collect(java.util.stream.Collectors.toSet());
            requestedIds.removeAll(foundIds);
            throw new IllegalStateException("Specialization(s) not found with id(s): " + requestedIds);
        }

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecializations(new HashSet<>(foundSpecs));

        boolean isGp = foundSpecs.stream().anyMatch(s -> "GP".equalsIgnoreCase(s.getType()));
        doctor.setGp(isGp);

        userRepository.save(user);
        doctorRepository.save(doctor);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private Set<Allergy> toAllergies(Set<AllergyDtoRequest> allergiesDtoRequests) {
        Set<Allergy> allergySet = new HashSet<>();

        for (AllergyDtoRequest allergyDtoRequest : allergiesDtoRequests) {
            Allergy allergy = new Allergy();
            allergy.setAllergen(allergyDtoRequest.getAllergen());
            allergySet.add(allergy);
        }

        return allergySet;
    }
}