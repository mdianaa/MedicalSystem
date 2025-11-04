package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.SickLeaveDtoRequest;
import org.nbu.medicalrecord.dtos.response.SickLeaveDtoResponse;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.entities.SickLeave;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.repositories.SickLeaveRepository;
import org.nbu.medicalrecord.services.SickLeaveService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SickLeaveServiceImpl implements SickLeaveService {

    private final SickLeaveRepository sickLeaveRepo;
    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepo;

    @Override
    @Transactional
    public SickLeaveDtoResponse createSickLeave(SickLeaveDtoRequest req) {
        if (req.getFromDate().isAfter(req.getToDate())) {
            throw new IllegalArgumentException("'fromDate' must be on/before 'toDate'.");
        }
        if (req.getToDate().isBefore(LocalDate.now().minusYears(1))) {
            throw new IllegalArgumentException("Sick leave is too far in the past.");
        }

        Doctor doctor = doctorRepo.findById(req.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor with id " + req.getDoctorId() + " not found"));
        Patient patient = patientRepo.findById(req.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient with id " + req.getPatientId() + " not found"));

        SickLeave sl = new SickLeave();
        sl.setDoctor(doctor);
        sl.setPatient(patient);
        sl.setFromDate(req.getFromDate());
        sl.setToDate(req.getToDate());
        sl.setReason(req.getReason());

        sickLeaveRepo.save(sl);
        return toDto(sl);
    }

    @Override
    public Set<SickLeaveDtoResponse> showAllSickLeavesByDoctor(long doctorId) {
        return sickLeaveRepo.findByDoctor_IdOrderByFromDateDesc(doctorId).stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<SickLeaveDtoResponse> showAllSickLeavesForPatient(long patientId) {
        return sickLeaveRepo.findByPatient_IdOrderByFromDateDesc(patientId).stream()
                .map(this::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Month showMonthWithMostSickLeaves() {
        var list = sickLeaveRepo.countByMonthDesc();
        if (list.isEmpty()) return null;
        int monthNumber = list.getFirst().getM();
        return Month.of(monthNumber);
    }

    private SickLeaveDtoResponse toDto(SickLeave s) {
        var d = s.getDoctor();
        var p = s.getPatient();

        String doctorName = (d == null) ? null
                : (d.getUser().getFirstName() + " " + d.getUser().getLastName());

        String patientName = (p == null) ? null
                : (p.getUser() != null ? p.getUser().getFirstName() + " " + p.getUser().getLastName()
                : null);

        int days = (int) (ChronoUnit.DAYS.between(s.getFromDate(), s.getToDate()) + 1);

        return new SickLeaveDtoResponse(
                s.getId(),
                d != null ? d.getId() : null,
                doctorName,
                p != null ? p.getId() : null,
                patientName,
                s.getFromDate(),
                s.getToDate(),
                s.getReason(),
                days
        );
    }
}
