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
@RequestMapping("/patients/{patientId}/health-insurances")
@RequiredArgsConstructor
public class HealthInsuranceController {

    private final HealthInsuranceService healthInsuranceService;

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

    @PostMapping("/pay")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public ResponseEntity<Void> paySingle(
            @PathVariable Long patientId,
            @RequestParam Month month,
            @RequestParam int year
    ) {
        healthInsuranceService.payHealthInsuranceForMonthInYear(patientId, month, year);
        return ResponseEntity.noContent().build();
    }

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

    @GetMapping("/last-six")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public Set<HealthInsuranceDtoResponse> lastSix(@PathVariable Long patientId) {
        return healthInsuranceService.referenceForLastSixMonthsByPatientId(patientId);
    }
}

