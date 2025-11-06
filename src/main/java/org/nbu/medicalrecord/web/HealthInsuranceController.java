package org.nbu.medicalrecord.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.HealthInsuranceDtoRequest;
import org.nbu.medicalrecord.dtos.response.HealthInsuranceDtoResponse;
import org.nbu.medicalrecord.services.HealthInsuranceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Month;
import java.util.Set;

@RestController
@RequestMapping("/patient/{patientId}/health-insurance")
@RequiredArgsConstructor
public class HealthInsuranceController {

    private final HealthInsuranceService healthInsuranceService;

    // Create health insurance for a patient
    @PostMapping
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public ResponseEntity<HealthInsuranceDtoResponse> create(
            @PathVariable Long patientId,
            @Valid @RequestBody HealthInsuranceDtoRequest req
    ) {
        HealthInsuranceDtoRequest fixed = new HealthInsuranceDtoRequest(patientId, req.getMonth(), req.getYear());
        HealthInsuranceDtoResponse res = healthInsuranceService.createNewHealthInsurance(fixed);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // Pay a single health insurance
    // TODO
    @PostMapping("/pay/{month}/{year}")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public ResponseEntity<Void> paySingle(
            @PathVariable Long patientId,
            @RequestParam Month month,
            @RequestParam int year
    ) {
        healthInsuranceService.payHealthInsuranceForMonthInYear(patientId, month, year);
        return ResponseEntity.noContent().build();
    }

    // Pay a couple of health insurances
    // TODO
    @PostMapping("/pay/bulk")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public ResponseEntity<Void> payBulk(
            @PathVariable Long patientId,
            @RequestParam Set<Month> months,
            @RequestParam int year
    ) {
        healthInsuranceService.payHealthInsuranceForMonthsInYear(patientId, months, year);
        return ResponseEntity.noContent().build();
    }

    // Get reference for the last six paid health insurances by a patient
    @GetMapping("/last-six")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public Set<HealthInsuranceDtoResponse> lastSix(@PathVariable Long patientId) {
        return healthInsuranceService.referenceForLastSixMonthsByPatientId(patientId);
    }

    // Get all health insurances for a patient
    @GetMapping("/all")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public Set<HealthInsuranceDtoResponse> all(@PathVariable Long patientId) {
        return healthInsuranceService.findAllByPatient(patientId);
    }

    // Get only paid health insurances for patient
    @GetMapping("/paid")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public Set<HealthInsuranceDtoResponse> paid(@PathVariable Long patientId) {
        return healthInsuranceService.findAllPaidByPatient(patientId);
    }

    // Get only unpaid health insurances for patient
    @GetMapping("/unpaid")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public Set<HealthInsuranceDtoResponse> unpaid(@PathVariable Long patientId) {
        return healthInsuranceService.findAllUnpaidByPatient(patientId);
    }
}

