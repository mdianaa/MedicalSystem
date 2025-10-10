package org.nbu.medicalrecord.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.DoctorsScheduleDtoRequest;
import org.nbu.medicalrecord.dtos.response.DoctorsScheduleDtoResponse;
import org.nbu.medicalrecord.services.DoctorsScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doctors/{doctorId}/schedule")
@RequiredArgsConstructor
public class DoctorsScheduleController {

    private final DoctorsScheduleService scheduleService;

    // Upsert a doctor's schedule (shift)
    @PutMapping
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public ResponseEntity<DoctorsScheduleDtoResponse> upsert(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorsScheduleDtoRequest req) {

        DoctorsScheduleDtoRequest fixed = new DoctorsScheduleDtoRequest(doctorId, req.getShift());
        DoctorsScheduleDtoResponse res = scheduleService.createNewSchedule(fixed);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long doctorId,
            @PathVariable Long scheduleId
    ) {
        scheduleService.deleteSchedule(doctorId, scheduleId);
        return ResponseEntity.noContent().build();
    }
}
