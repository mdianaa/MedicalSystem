package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.HealthInsuranceDtoRequest;
import org.nbu.medicalrecord.dtos.response.HealthInsuranceDtoResponse;
import org.nbu.medicalrecord.entities.HealthInsurance;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.repositories.HealthInsuranceRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.HealthInsuranceService;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HealthInsuranceServiceImpl implements HealthInsuranceService {
    private final HealthInsuranceRepository repo;
    private final PatientRepository patientRepo;

    @Override
    @Transactional
    public HealthInsuranceDtoResponse createNewHealthInsurance(HealthInsuranceDtoRequest req) {
        Patient patient = patientRepo.findById(req.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        if (repo.existsByPatient_IdAndMonthAndYear(patient.getId(), req.getMonth(), req.getYear())) {
            throw new IllegalStateException("Entry already exists for that month/year.");
        }

        HealthInsurance hi = new HealthInsurance();
        hi.setPatient(patient);
        hi.setMonth(req.getMonth());
        hi.setYear(req.getYear());
        hi.setPaid(req.isPaid());

        repo.save(hi);
        return toDto(hi);
    }

    @Override
    @Transactional
    public void payHealthInsuranceForMonthInYear(long patientId, Month month, int year) {
        HealthInsurance hi = repo.findByPatient_IdAndMonthAndYear(patientId, month, year)
                .orElseThrow(() -> new IllegalArgumentException("Health insurance entry not found"));
        if (!hi.isPaid()) {
            hi.setPaid(true);
            repo.save(hi);
        }
    }

    @Override
    @Transactional
    public void payHealthInsuranceForMonthsInYear(long patientId, Set<Month> months, int year) {
        for (Month m : months) {
            HealthInsurance hi = repo.findByPatient_IdAndMonthAndYear(patientId, m, year)
                    .orElseThrow(() -> new IllegalArgumentException("Entry not found for " + m + " " + year));
            if (!hi.isPaid()) {
                hi.setPaid(true);
                repo.save(hi);
            }
        }
    }

    @Override
    public Set<HealthInsuranceDtoResponse> referenceForLastSixMonthsByPatientId(Long patientId) {
        return repo.findAllByPatientOrderByYearMonthDesc(patientId).stream()
                .limit(6)
                .map(this::toDto)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private HealthInsuranceDtoResponse toDto(HealthInsurance hi) {
        return new HealthInsuranceDtoResponse(
                hi.getId(),
                hi.getPatient().getId(),
                hi.getMonth(),
                hi.getYear(),
                hi.isPaid()
        );
    }
}