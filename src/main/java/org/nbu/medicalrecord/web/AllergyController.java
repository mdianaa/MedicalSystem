package org.nbu.medicalrecord.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.AllergyDtoRequest;
import org.nbu.medicalrecord.dtos.response.AllergyDtoResponse;
import org.nbu.medicalrecord.services.AllergyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/allergy")
@RequiredArgsConstructor
public class AllergyController {

    private final AllergyService allergyService;

    // Only ADMIN can manage vocabulary by default
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AllergyDtoResponse> add(@Valid @RequestBody AllergyDtoRequest req) {
        AllergyDtoResponse res = allergyService.addAllergy(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // Doctors/Admins can read details
    @GetMapping("/{allergen}")
    @PreAuthorize("hasAnyAuthority('DOCTOR','ADMIN')")
    public AllergyDtoResponse show(@PathVariable String allergen) {
        return allergyService.showAllergy(allergen);
    }

    // Only ADMIN can delete by default
    @DeleteMapping("/{allergen}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String allergen) {
        allergyService.deleteAllergy(allergen);
        return ResponseEntity.noContent().build();
    }
}