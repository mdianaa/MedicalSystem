package org.nbu.medicalrecord.services.impl;

import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.response.DoctorDataPatientViewDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientDataDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientDataWithDoctorDtoResponse;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.PatientService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public Set<PatientDataWithDoctorDtoResponse> showAllPatientsWithGP(long doctorId) {
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor with id " + doctorId + " not found"));

        return patientRepository.findByGp_Id(doctorId).stream()
                .sorted(byNameThenId())
                .map(this::toPatientWithDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public int totalCountPatientsWithGP(long doctorId) {
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor with id " + doctorId + " not found"));

        return patientRepository.countByGp_Id(doctorId);
    }

    @Override
    public Set<PatientDataWithDoctorDtoResponse> showAllPatients() {
        return patientRepository.findAll().stream()
                .sorted(byNameThenId())
                .map(this::toPatientWithDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<PatientDataWithDoctorDtoResponse> showAllPatientsWhoVisitedDoctor(long doctorId) {
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor with id " + doctorId + " not found"));

        return patientRepository.findDistinctByVisitedDoctor(doctorId).stream()
                .sorted(byNameThenId())
                .map(this::toPatientWithDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public int totalCountPatientsWhoVisitedDoctor(long doctorId) {
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor with id " + doctorId + " not found"));

        return patientRepository.countDistinctByVisitedDoctor(doctorId);
    }

    @Override
    public Set<PatientDataWithDoctorDtoResponse> showAllPatientsWithResultDiagnosis(String result) {
        return patientRepository.findDistinctByDiagnosisResult(result).stream()
                .sorted(byNameThenId())
                .map(this::toPatientWithDoctorDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public int totalCountPatientsWithResultDiagnosis(String result) {
        return patientRepository.countDistinctByDiagnosisResult(result);
    }

    @Override
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
            String specName = d.getSpecialization() != null ? d.getSpecialization().getType() : null;
            doctor = new DoctorDataPatientViewDtoResponse(
                    d.getId(),
                    d.getUser() != null ? d.getUser().getFirstName() : null,
                    d.getUser() != null ? d.getUser().getLastName() : null,
                    specName,
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
