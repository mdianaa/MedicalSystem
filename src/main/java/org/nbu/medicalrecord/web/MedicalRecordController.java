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
@RequestMapping("/medical-record")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    // Create Medical Record for a patient
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('DOCTOR')")
    public ResponseEntity<MedicalRecordDtoResponse> create(@Valid @RequestBody MedicalRecordDtoRequest req) {
        MedicalRecordDtoResponse res = medicalRecordService.createNewMedicalRecord(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // Show medical record for a patient
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) "
            + "or @authz.isDoctorOfPatient(authentication, #patientId) "
            + "or hasAuthority('ADMIN')")
    public MedicalRecordDtoResponse byPatient(@PathVariable long patientId) {
        return medicalRecordService.showMedicalRecord(patientId);
    }

    // Show all medical records
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Set<MedicalRecordDtoResponse> listAll() {
        return medicalRecordService.showAllMedicalRecords();
    }

    // Delete medical record
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        medicalRecordService.deleteMedicalRecord(id);
        return ResponseEntity.noContent().build();
    }
}

