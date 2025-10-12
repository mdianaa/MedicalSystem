package org.nbu.medicalrecord.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.AppointmentDtoRequest;
import org.nbu.medicalrecord.dtos.response.DoctorAppointmentDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientAppointmentDtoResponse;
import org.nbu.medicalrecord.services.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // PATIENT creates an appointment with a doctor (by IDs)
    @PostMapping
    @PreAuthorize("@authz.isPatient(authentication, #req.patientId()) or hasAuthority('ADMIN')")
    public ResponseEntity<PatientAppointmentDtoResponse> make(@Valid @RequestBody AppointmentDtoRequest req) {
        PatientAppointmentDtoResponse res = appointmentService.makeAppointment(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // PATIENT/DOCTOR/Admin can view available slots for a specific doctor
    @GetMapping("/doctor/{doctorId}/available")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAnyAuthority('PATIENT','ADMIN')")
    public Set<PatientAppointmentDtoResponse> availableByDoctor(@PathVariable Long doctorId) {
        return appointmentService.showAllAvailableAppointmentsByDoctorId(doctorId);
    }

    // DOCTOR/Admin view booked (occupied) appointments for a doctor
    @GetMapping("/doctor/{doctorId}/occupied")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public Set<DoctorAppointmentDtoResponse> occupiedByDoctor(@PathVariable Long doctorId) {
        return appointmentService.showAllOccupiedAppointmentsById(doctorId);
    }

    // PATIENT/Admin view all appointments for a patient
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public Set<PatientAppointmentDtoResponse> patientAppointments(@PathVariable Long patientId) {
        return appointmentService.showAllPatientAppointmentsById(patientId);
    }

    // PATIENT/Admin view a patient's appointment on a specific date (YYYY-MM-DD)
    @GetMapping("/patient/{patientId}/on/{date}")
    @PreAuthorize("@authz.isPatient(authentication, #patientId) or hasAuthority('ADMIN')")
    public PatientAppointmentDtoResponse patientAppointmentOn(
            @PathVariable Long patientId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointmentService.showPatientAppointmentOnDateById(patientId, date);
    }

    // Cancel by id — tighten with an ownership helper (@authz.canCancelAppointment) if you have one
    @DeleteMapping("/{appointmentId}")
    @PreAuthorize("@authz.canCancelAppointment(authentication, #appointmentId) or hasAuthority('ADMIN')")
    public ResponseEntity<Void> cancel(@PathVariable long appointmentId) {
        appointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }
}