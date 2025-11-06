package org.nbu.medicalrecord.web;

import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.response.PatientDataWithDoctorDtoResponse;
import org.nbu.medicalrecord.services.PatientService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // Get all patients
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    public Set<PatientDataWithDoctorDtoResponse> all() {
        return patientService.showAllPatients();
    }

    // Get patients for with GP (doctor can see own GP panel; admin can see any)
    @GetMapping("/gp/{doctorId}")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public Set<PatientDataWithDoctorDtoResponse> byGp(@PathVariable long doctorId) {
        return patientService.showAllPatientsWithGP(doctorId);
    }

    // Patients who visited a doctor (derived from diagnoses)
    @GetMapping("/visited/{doctorId}")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public Set<PatientDataWithDoctorDtoResponse> visited(@PathVariable long doctorId) {
        return patientService.showAllPatientsWhoVisitedDoctor(doctorId);
    }

    // Get the total count of the patients who visited a particular doctor
    @GetMapping("/visited/{doctorId}/count")
    @PreAuthorize("@authz.isDoctor(authentication, #doctorId) or hasAuthority('ADMIN')")
    public String visitedCount(@PathVariable long doctorId) {
        return "Total count of patients visited doctor with id " + doctorId + " : " + patientService.totalCountPatientsWhoVisitedDoctor(doctorId);
    }

    // Patients filtered by a diagnosis result
    // TODO
    @GetMapping("/by-diagnosis-result")
    @PreAuthorize("hasAnyAuthority('DOCTOR','ADMIN')")
    public Set<PatientDataWithDoctorDtoResponse> byDiagnosisResult(@RequestParam String result) {
        return patientService.showAllPatientsWithResultDiagnosis(result);
    }

    // Get count of all patients with the same diagnosis
    // TODO
    @GetMapping("/by-diagnosis-result/count")
    @PreAuthorize("hasAnyAuthority('DOCTOR','ADMIN')")
    public int byDiagnosisResultCount(@RequestParam String result) {
        return patientService.totalCountPatientsWithResultDiagnosis(result);
    }

    // Patients with the same allergen
    @GetMapping("/{allergen}")
    @PreAuthorize("hasAnyAuthority('DOCTOR','ADMIN')")
    public Set<PatientDataWithDoctorDtoResponse> byAllergy(@PathVariable String allergen) {
        return patientService.showAllPatientsWithAllergy(allergen);
    }
}