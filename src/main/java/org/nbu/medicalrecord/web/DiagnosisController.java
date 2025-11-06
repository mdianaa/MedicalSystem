package org.nbu.medicalrecord.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.DiagnosisDtoRequest;
import org.nbu.medicalrecord.dtos.response.DiagnosisDtoResponse;
import org.nbu.medicalrecord.services.DiagnosisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    // Doctor creates a diagnosis for a patient
    @PostMapping
    @PreAuthorize("@authz.isDoctor(authentication, #req.doctorId) or hasAuthority('ADMIN')")
    public ResponseEntity<DiagnosisDtoResponse> create(@Valid @RequestBody DiagnosisDtoRequest req) {
        DiagnosisDtoResponse res = diagnosisService.createDiagnosis(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // View patient’s diagnoses
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public Set<DiagnosisDtoResponse> byPatient(@PathVariable Long patientId) {
        return diagnosisService.showAllDiagnosisForPatientId(patientId);
    }

    // View doctor’s diagnoses
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public Set<DiagnosisDtoResponse> byDoctor(@PathVariable Long doctorId) {
        return diagnosisService.showAllDiagnosisByDoctorId(doctorId);
    }

    // Top diagnosis result
    // TODO
    @GetMapping("/most-frequent")
    @PreAuthorize("hasAnyAuthority('DOCTOR','ADMIN')")
    public Set<DiagnosisDtoResponse> mostFrequent() {
        return diagnosisService.showMostFrequentDiagnosisResult();
    }

    // Delete diagnosis
    @DeleteMapping("/{diagnosisId}")
    @PreAuthorize("@authz.isDiagnosisOwner(authentication, #diagnosisId) or hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long diagnosisId) {
        diagnosisService.deleteDiagnosis(diagnosisId);
        return ResponseEntity.noContent().build();
    }
}
