package org.nbu.medicalrecord.services.impl;

import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.response.DoctorDataPatientViewDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientDataDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientDataWithDoctorDtoResponse;
import org.nbu.medicalrecord.dtos.response.SpecializationDtoResponse;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.nbu.medicalrecord.util.CheckExistUtil.*;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional
    public Set<PatientDataWithDoctorDtoResponse> showAllPatientsWithGP(long doctorId) {
        checkIfDoctorExists(doctorRepository, doctorId);

        return patientRepository.findByGp_Id(doctorId).stream()
                .sorted(byNameThenId())
                .map(this::toPatientWithDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public Set<PatientDataWithDoctorDtoResponse> showAllPatients() {
        return patientRepository.findAll().stream()
                .sorted(byNameThenId())
                .map(this::toPatientWithDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public Set<PatientDataWithDoctorDtoResponse> showAllPatientsWhoVisitedDoctor(long doctorId) {
        checkIfDoctorExists(doctorRepository, doctorId);

        return patientRepository.findDistinctByVisitedDoctor(doctorId).stream()
                .sorted(byNameThenId())
                .map(this::toPatientWithDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public int totalCountPatientsWhoVisitedDoctor(long doctorId) {
        checkIfPatientExists(patientRepository, doctorId);

        return patientRepository.countDistinctByVisitedDoctor(doctorId);
    }

    @Override
    @Transactional
    public Set<PatientDataWithDoctorDtoResponse> showAllPatientsWithSameDiagnosis(String diagnosis) {
        // TODO check if diagnosis exists

        return patientRepository.findDistinctByDiagnosis(diagnosis).stream()
                .sorted(byNameThenId())
                .map(this::toPatientWithDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Transactional
    public int totalCountPatientsWithSameDiagnosis(String diagnosis) {
        // TODO check if diagnosis exists

        return patientRepository.countDistinctByDiagnosis(diagnosis);
    }

    @Override
    @Transactional
    public Set<PatientDataWithDoctorDtoResponse> showAllPatientsWithAllergy(String allergen) {
        return patientRepository.findDistinctByAllergen(allergen).stream()
                .sorted(byNameThenId())
                .map(this::toPatientWithDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Comparator<Patient> byNameThenId() {
        return Comparator
                .comparing(this::safeFirstName, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(this::safeLastName, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(Patient::getId);
    }

    private PatientDataWithDoctorDtoResponse toPatientWithDoctorDto(Patient p) {
        PatientDataDtoResponse patient = new PatientDataDtoResponse(
                p.getId(),
                p.getUser() != null ? p.getUser().getFirstName() : null,
                p.getUser() != null ? p.getUser().getLastName() : null,
                p.getBirthDate()
        );

        DoctorDataPatientViewDtoResponse doctor = getDoctorDto(p);

        return new PatientDataWithDoctorDtoResponse(patient, doctor);
    }

    private static DoctorDataPatientViewDtoResponse getDoctorDto(Patient p) {
        Doctor d = p.getGp();
        DoctorDataPatientViewDtoResponse doctor = null;
        if (d != null) {
            Set<SpecializationDtoResponse> specDtos =
                    (d.getSpecializations() == null)
                            ? Set.of()
                            : d.getSpecializations().stream()
                            .map(s -> new SpecializationDtoResponse(s.getId(), s.getType()))
                            .collect(Collectors.toSet());

            doctor = new DoctorDataPatientViewDtoResponse(
                    d.getId(),
                    d.getUser() != null ? d.getUser().getFirstName() : null,
                    d.getUser() != null ? d.getUser().getLastName() : null,
                    specDtos,
                    d.isGp()
            );
        }
        return doctor;
    }

    private String safeFirstName(Patient p) {
        return p.getUser() != null ? p.getUser().getFirstName() : null;
    }
    private String safeLastName(Patient p) {
        return p.getUser() != null ? p.getUser().getLastName() : null;
    }
}
