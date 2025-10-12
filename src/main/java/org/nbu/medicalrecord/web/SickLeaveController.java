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
@RequestMapping("/sick-leaves")
@RequiredArgsConstructor
public class SickLeaveController {

    private final SickLeaveService sickLeaveService;

    // Create (doctor issues sick leave)
    @PostMapping
    @PreAuthorize("@authz.isDoctor(authentication, #req.doctorId()) or hasAuthority('ADMIN')")
    public ResponseEntity<SickLeaveDtoResponse> create(@Valid @RequestBody SickLeaveDtoRequest req) {
        SickLeaveDtoResponse res = sickLeaveService.createSickLeave(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // Doctor’s view
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public Set<SickLeaveDtoResponse> byDoctor(@PathVariable long doctorId) {
        return sickLeaveService.showAllSickLeavesByDoctor(doctorId);
    }

    // Patient’s view
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public Set<SickLeaveDtoResponse> byPatient(@PathVariable long patientId) {
        return sickLeaveService.showAllSickLeavesForPatient(patientId);
    }

    // Analytics: month with most sick leaves (overall)
    @GetMapping("/most-active-month")
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    public Month mostActiveMonth() {
        return sickLeaveService.showMonthWithMostSickLeaves();
    }
}
