package org.nbu.medicalrecord.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.SickLeaveDtoRequest;
import org.nbu.medicalrecord.dtos.response.SickLeaveDtoResponse;
import org.nbu.medicalrecord.services.SickLeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Month;
import java.util.Set;

@RestController
@RequestMapping("/sick-leave")
@RequiredArgsConstructor
public class SickLeaveController {

    private final SickLeaveService sickLeaveService;

    // Create sick leave (doctor issues sick leave)
    @PostMapping
    @PreAuthorize("@authz.isDoctor(authentication, #req.doctorId) or hasAuthority('ADMIN')")
    public ResponseEntity<SickLeaveDtoResponse> create(@Valid @RequestBody SickLeaveDtoRequest req) {
        SickLeaveDtoResponse res = sickLeaveService.createSickLeave(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // Show all sick leaves prescribed by doctor
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public Set<SickLeaveDtoResponse> byDoctor(@PathVariable long doctorId) {
        return sickLeaveService.showAllSickLeavesByDoctor(doctorId);
    }

    // Show all sick leaves prescribed to a patient
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public Set<SickLeaveDtoResponse> byPatient(@PathVariable long patientId) {
        return sickLeaveService.showAllSickLeavesForPatient(patientId);
    }

    // Get month with most sick leaves
    @GetMapping("/most-active-month")
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    public Month mostActiveMonth() {
        return sickLeaveService.showMonthWithMostSickLeaves();
    }
}
