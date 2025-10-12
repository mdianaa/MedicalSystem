package org.nbu.medicalrecord.web;

import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.response.PatientDataWithDoctorDtoResponse;
import org.nbu.medicalrecord.services.PatientService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // All patients (admin/doctor)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    public Set<PatientDataWithDoctorDtoResponse> all() {
        return patientService.showAllPatients();
    }

    // Patients for a GP (doctor can see own GP panel; admin can see any)
    @GetMapping("/gp/{doctorId}")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public Set<PatientDataWithDoctorDtoResponse> byGp(@PathVariable long doctorId) {
        return patientService.showAllPatientsWithGP(doctorId);
    }

    @GetMapping("/gp/{doctorId}/count")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public int countByGp(@PathVariable long doctorId) {
        return patientService.totalCountPatientsWithGP(doctorId);
    }

    // Patients who visited a doctor (derived from diagnoses)
    @GetMapping("/visited/{doctorId}")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public Set<PatientDataWithDoctorDtoResponse> visited(@PathVariable long doctorId) {
        return patientService.showAllPatientsWhoVisitedDoctor(doctorId);
    }

    @GetMapping("/visited/{doctorId}/count")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public int visitedCount(@PathVariable long doctorId) {
        return patientService.totalCountPatientsWhoVisitedDoctor(doctorId);
    }

    // Patients filtered by a diagnosis result
    @GetMapping("/by-diagnosis-result")
    @PreAuthorize("hasAnyAuthority('DOCTOR','ADMIN')")
    public Set<PatientDataWithDoctorDtoResponse> byDiagnosisResult(@RequestParam String result) {
        return patientService.showAllPatientsWithResultDiagnosis(result);
    }

    @GetMapping("/by-diagnosis-result/count")
    @PreAuthorize("hasAnyAuthority('DOCTOR','ADMIN')")
    public int byDiagnosisResultCount(@RequestParam String result) {
        return patientService.totalCountPatientsWithResultDiagnosis(result);
    }

    // Patients with a specific allergen
    @GetMapping("/by-allergy")
    @PreAuthorize("hasAnyAuthority('DOCTOR','ADMIN')")
    public Set<PatientDataWithDoctorDtoResponse> byAllergy(@RequestParam String allergen) {
        return patientService.showAllPatientsWithAllergy(allergen);
    }
}