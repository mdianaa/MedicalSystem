package org.nbu.medicalrecord.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.AdminCreateDoctorRequest;
import org.nbu.medicalrecord.dtos.request.AdminCreatePatientRequest;
import org.nbu.medicalrecord.services.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/create-user")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    // registration logic from the admin

    private final RegistrationService registration;

    @PostMapping("/patient")
    public ResponseEntity<Void> createPatient(@Valid @RequestBody AdminCreatePatientRequest req) {
        registration.createPatient(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/doctor")
    public ResponseEntity<Void> createDoctor(@Valid @RequestBody AdminCreateDoctorRequest req) {
        registration.createDoctor(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
