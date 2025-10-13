package org.nbu.medicalrecord.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.VisitDtoRequest;
import org.nbu.medicalrecord.dtos.response.VisitDtoResponse;
import org.nbu.medicalrecord.services.VisitService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    // Create a visit for a completed appointment (doctor/admin)
    @PostMapping
    @PreAuthorize("@authz.isDoctorOfAppointment(authentication, #req.appointmentId()) or hasAuthority('ADMIN')")
    public ResponseEntity<VisitDtoResponse> create(@Valid @RequestBody VisitDtoRequest req) {
        VisitDtoResponse res = visitService.createNewVisit(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // Doctor’s view
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public Set<VisitDtoResponse> byDoctor(@PathVariable long doctorId) {
        return visitService.showAllVisitsByDoctor(doctorId);
    }

    // Patient’s view
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public Set<VisitDtoResponse> byPatient(@PathVariable long patientId) {
        return visitService.showAllVisitsForPatient(patientId);
    }

    @GetMapping("/patient/{patientId}/period")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public Set<VisitDtoResponse> patientInPeriod(
            @PathVariable long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return visitService.showAllVisitsForPatientInPeriod(patientId, from, to);
    }

    @GetMapping("/doctor/{doctorId}/period")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public Set<VisitDtoResponse> doctorInPeriod(
            @PathVariable long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return visitService.showAllVisitsByDoctorInPeriod(doctorId, from, to);
    }
}