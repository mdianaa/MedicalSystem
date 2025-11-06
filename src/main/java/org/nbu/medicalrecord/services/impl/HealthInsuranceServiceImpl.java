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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
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
                .orElseThrow(() -> new IllegalArgumentException("Patient with id " + req.getPatientId() + " not found"));

        boolean exists = repo.existsByPatient_IdAndMonthAndYear(
                patient.getId(),
                req.getMonth(),
                req.getYear()
        );
        if (exists) {
            throw new IllegalStateException("Entry already exists for that month/year.");
        }

        HealthInsurance hi = new HealthInsurance();
        hi.setPatient(patient);
        hi.setMonth(req.getMonth());
        hi.setYear(req.getYear());
        // unpaid at creation time
        hi.setPaid(false);

        repo.save(hi);
        return toDto(hi);
    }

    @Override
    /**
     * Runs at 01:00 on the 1st day of every month.
     * cron format = second minute hour day-of-month month day-of-week
     * "0 0 1 1 * *" -> at 01:00 on day 1 of each month
     */
    @Scheduled(cron = "0 0 1 1 * *")
    @Transactional
    public void createMonthlyHealthInsuranceRows() {

        LocalDate now = LocalDate.now();
        Month month = now.getMonth();
        int year = now.getYear();

        // get all patients
        List<Patient> patients = patientRepo.findAll();

        // for each patient, ensure the row for (month, year) exists
        for (Patient p : patients) {
            boolean exists = repo.existsByPatient_IdAndMonthAndYear(
                    p.getId(),
                    month,
                    year
            );

            if (!exists) {
                HealthInsurance hi = new HealthInsurance();
                hi.setPatient(p);
                hi.setMonth(month);
                hi.setYear(year);
                hi.setPaid(false);

                repo.save(hi);
            }
        }
    }

    @Override
    @Transactional
    public void payHealthInsuranceForMonthInYear(long patientId, Month month, int year) {
        HealthInsurance hi = repo.findByPatient_IdAndMonthAndYear(patientId, month, year)
                .orElseThrow(() -> new IllegalArgumentException("Health insurance entry not found"));

        if (hi.isPaid()) {
            throw new IllegalStateException("Already paid for " + month + " " + year);
        }

        hi.setPaid(true);
        repo.save(hi);
    }

    @Override
    @Transactional
    public void payHealthInsuranceForMonthsInYear(long patientId, Set<Month> months, int year) {
        for (Month m : months) {
            HealthInsurance hi = repo.findByPatient_IdAndMonthAndYear(patientId, m, year)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Entry not found for " + m + " " + year));

            if (hi.isPaid()) {
                throw new IllegalStateException("Already paid for " + m + " " + year);
            }

            hi.setPaid(true);
            repo.save(hi);
        }
    }

    @Override
    @Transactional
    public Set<HealthInsuranceDtoResponse> findAllByPatient(Long patientId) {
        if (patientRepo.findById(patientId).isEmpty()) {
            throw new IllegalArgumentException("Patient with id " + patientId + " not found");
        }
        return repo.findAllByPatientOrderByYearCalendarMonthDesc(patientId).stream()
                .map(this::toDto)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    @Override
    @Transactional
    public Set<HealthInsuranceDtoResponse> findAllPaidByPatient(Long patientId) {
        if (patientRepo.findById(patientId).isEmpty()) {
            throw new IllegalArgumentException("Patient with id " + patientId + " not found");
        }
        return repo.findPaidByPatientOrderByYearMonthDesc(patientId).stream()
                .map(this::toDto)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    @Override
    @Transactional
    public Set<HealthInsuranceDtoResponse> findAllUnpaidByPatient(Long patientId) {
        if (patientRepo.findById(patientId).isEmpty()) {
            throw new IllegalArgumentException("Patient with id " + patientId + " not found");
        }
        return repo.findUnpaidByPatientOrderByYearMonthDesc(patientId).stream()
                .map(this::toDto)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
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