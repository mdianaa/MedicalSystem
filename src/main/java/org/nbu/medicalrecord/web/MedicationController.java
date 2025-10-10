package org.nbu.medicalrecord.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.MedicationDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicationDtoResponse;
import org.nbu.medicalrecord.services.MedicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    // Typically created via Diagnosis, but keeping this for completeness/tools
    @PostMapping
    @PreAuthorize("hasAnyAuthority('DOCTOR','ADMIN')")
    public ResponseEntity<MedicationDtoResponse> add(@Valid @RequestBody MedicationDtoRequest req) {
        var res = medicationService.addMedication(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('DOCTOR','ADMIN')")
    public Set<MedicationDtoResponse> list() {
        return medicationService.showAllMedications();
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public Set<MedicationDtoResponse> byDoctor(@PathVariable long doctorId) {
        return medicationService.showAllMedicationsByDoctor(doctorId);
    }
}