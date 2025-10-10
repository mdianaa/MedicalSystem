package org.nbu.medicalrecord.web;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.response.DoctorDataDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientDataDtoResponse;
import org.nbu.medicalrecord.services.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // Add patient to a GP panel
    @PostMapping("/{doctorId}/gp/patients/{patientId}")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public ResponseEntity<PatientDataDtoResponse> addPatientForGp(@PathVariable Long doctorId, @PathVariable Long patientId) {
        var res = doctorService.addNewPatientForGpById(patientId, doctorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // Count GP panel size
    @GetMapping("/{doctorId}/gp/patients/count")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public int countGpPatients(@PathVariable Long doctorId) {
        return doctorService.countTotalPatientsForGpById(doctorId);
    }

    // List GPs
    @GetMapping("/gps")
    @PreAuthorize("hasAnyAuthority('ADMIN','PATIENT','DOCTOR')")
    public Set<DoctorDataDtoResponse> listGps() {
        return doctorService.showAllGPs();
    }

    // List all doctors
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','PATIENT','DOCTOR')")
    public Set<DoctorDataDtoResponse> listAll() {
        return doctorService.showAllDoctors();
    }

    // Filter by specialization (string value)
    @GetMapping("/specialization/{type}")
    @PreAuthorize("hasAnyAuthority('ADMIN','PATIENT','DOCTOR')")
    public Set<DoctorDataDtoResponse> bySpecialization(@PathVariable @NotBlank String type) {
        return doctorService.showAllDoctorsWithSpecialization(type);
    }

    // Doctors with most sick leaves given (ordered)
    @GetMapping("/top-sick-leaves")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public Set<DoctorDataDtoResponse> topSickLeaves() {
        return doctorService.showAllDoctorsWithMostSickLeavesGiven();
    }
}