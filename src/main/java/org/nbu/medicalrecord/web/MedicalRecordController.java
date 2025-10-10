package org.nbu.medicalrecord.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.MedicalRecordDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicalRecordDtoResponse;
import org.nbu.medicalrecord.services.MedicalRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService service;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('DOCTOR')") // tighten with GP rule if needed
    public ResponseEntity<MedicalRecordDtoResponse> create(@Valid @RequestBody MedicalRecordDtoRequest req) {
        var res = service.createNewMedicalRecord(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) "
            + "or @authz.isDoctorOfPatient(authentication, #patientId) "
            + "or hasAuthority('ADMIN')")
    public MedicalRecordDtoResponse byPatient(@PathVariable long patientId) {
        return service.showMedicalRecord(patientId);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Set<MedicalRecordDtoResponse> listAll() {
        return service.showAllMedicalRecords();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.deleteMedicalRecord(id);
        return ResponseEntity.noContent().build();
    }
}

