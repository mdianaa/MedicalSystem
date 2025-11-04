package org.nbu.medicalrecord.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.MedicineDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicineDtoResponse;
import org.nbu.medicalrecord.services.MedicineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/medicine")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<MedicineDtoResponse> add(@Valid @RequestBody MedicineDtoRequest req) {
        MedicineDtoResponse res = medicineService.addMedicine(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('DOCTOR','ADMIN')")
    public Set<MedicineDtoResponse> list() {
        return medicineService.showAllMedicines();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('DOCTOR','ADMIN')")
    public MedicineDtoResponse get(@RequestParam String name, @RequestParam int mg) {
        return medicineService.showMedicine(name, mg);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }
}
