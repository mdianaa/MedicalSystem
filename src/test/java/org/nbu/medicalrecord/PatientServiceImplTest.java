package org.nbu.medicalrecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.response.DoctorDataPatientViewDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientDataDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientDataWithDoctorDtoResponse;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.entities.Specialization;
import org.nbu.medicalrecord.entities.User;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.impl.PatientServiceImpl;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl service;

    private Doctor doctor(
            long id,
            String first,
            String last,
            String specializationType,
            boolean isGp
    ) {
        Doctor d = new Doctor();
        d.setId(id);
        User du = new User();
        du.setFirstName(first);
        du.setLastName(last);
        d.setUser(du);

        if (specializationType != null) {
            Specialization s = new Specialization();
            s.setType(specializationType);
            d.setSpecialization(s);
        }

        d.setGp(isGp);
        return d;
    }

    private Patient patient(
            long id,
            String first,
            String last,
            LocalDate birthDate,
            Doctor gpDoctor
    ) {
        Patient p = new Patient();
        p.setId(id);
        if (first != null || last != null) {
            User u = new User();
            u.setFirstName(first);
            u.setLastName(last);
            p.setUser(u);
        }
        p.setBirthDate(birthDate);
        p.setGp(gpDoctor);
        return p;
    }

    // ---------- showAllPatientsWithGP ----------
    @Test
    @DisplayName("showAllPatientsWithGP: returns patients for given GP id, sorted by firstName/lastName/id, mapped to PatientDataWithDoctorDtoResponse")
    void showAllPatientsWithGP_sortedAndMapped() {
        long doctorId = 5L;

        Doctor gp = doctor(
                doctorId,
                "Gregory",
                "House",
                "Diagnostic Medicine",
                true
        );

        Patient p1 = patient(3L, "Ana", "Zed", LocalDate.of(1990, 1, 1), gp);
        Patient p2 = patient(2L, "Ana", "Alpha", LocalDate.of(1992, 2, 2), gp);
        Patient p3 = patient(1L, "Boris", "Beta", LocalDate.of(1994, 3, 3), gp);

        // Out-of-order list from repo to test sorting
        when(patientRepository.findByGp_Id(doctorId)).thenReturn(List.of(p1, p3, p2));

        Set<PatientDataWithDoctorDtoResponse> out = service.showAllPatientsWithGP(doctorId);

        // verify repository call
        verify(patientRepository).findByGp_Id(doctorId);

        // order: firstName asc, then lastName asc, then id asc
        // p2 ("Ana Alpha", id=2) before p1 ("Ana Zed", id=3), then p3 ("Boris", ...)
        List<Long> orderedIds = out.stream()
                .map(dto -> dto.getPatientData().getId())
                .collect(Collectors.toList());
        assertThat(orderedIds).containsExactly(2L, 3L, 1L);

        // verify mapping of patient fields
        PatientDataWithDoctorDtoResponse firstDto = out.iterator().next();
        PatientDataDtoResponse patientPart = firstDto.getPatientData();
        assertThat(patientPart.getFirstName()).isEqualTo("Ana");
        assertThat(patientPart.getLastName()).isEqualTo("Alpha");
        assertThat(patientPart.getBirthDate()).isEqualTo(LocalDate.of(1992, 2, 2));

        // verify mapping of doctor part
        DoctorDataPatientViewDtoResponse doctorPart = firstDto.getDoctorData();
        assertThat(doctorPart).isNotNull();
        assertThat(doctorPart.getId()).isEqualTo(5L);
        assertThat(doctorPart.getFirstName()).isEqualTo("Gregory");
        assertThat(doctorPart.getLastName()).isEqualTo("House");
        assertThat(doctorPart.getSpecializationType()).isEqualTo("Diagnostic Medicine");
        assertThat(doctorPart.isGp()).isTrue();
    }

    // ---------- totalCountPatientsWithGP ----------
    @Test
    @DisplayName("totalCountPatientsWithGP delegates to repository.countByGp_Id")
    void totalCountPatientsWithGP_delegates() {
        when(patientRepository.countByGp_Id(10L)).thenReturn(7);

        int result = service.totalCountPatientsWithGP(10L);

        verify(patientRepository).countByGp_Id(10L);
        assertThat(result).isEqualTo(7);
    }

    // ---------- showAllPatients ----------
    @Test
    @DisplayName("showAllPatients: returns all patients sorted by name then id, mapped with GP data if present")
    void showAllPatients_sortedAndMapped() {
        Doctor gp1 = doctor(100L, "Laura", "Smith", "Cardiology", true);
        Doctor gp2 = doctor(200L, "John", "Reed", null, true); // specialization null ok

        Patient a1 = patient(10L, "Ana", "Bravo", LocalDate.of(1980, 5, 5), gp1);
        Patient a2 = patient(11L, "Ana", "Bravo", LocalDate.of(1982, 6, 6), gp2); // same name, different id -> id decides order
        Patient b  = patient(12L, "Boris", "Chan", LocalDate.of(1990, 7, 7), null);

        when(patientRepository.findAll()).thenReturn(List.of(b, a2, a1));

        Set<PatientDataWithDoctorDtoResponse> out = service.showAllPatients();

        // order: ("Ana","Bravo",id=10), ("Ana","Bravo",id=11), then ("Boris","Chan",id=12)
        List<Long> ids = out.stream().map(dto -> dto.getPatientData().getId()).toList();
        assertThat(ids).containsExactly(10L, 11L, 12L);

        // check GP doctor information is included for first element
        PatientDataWithDoctorDtoResponse first = out.iterator().next();
        assertThat(first.getDoctorData()).isNotNull();
        assertThat(first.getDoctorData().getId()).isEqualTo(100L);
        assertThat(first.getDoctorData().getSpecializationType()).isEqualTo("Cardiology");

        // patient with no GP should have doctor = null
        PatientDataWithDoctorDtoResponse last = out.stream()
                .filter(dto -> dto.getPatientData().getId().equals(12L))
                .findFirst()
                .orElseThrow();
        assertThat(last.getDoctorData()).isNull();
    }

    // ---------- showAllPatientsWhoVisitedDoctor ----------
    @Test
    @DisplayName("showAllPatientsWhoVisitedDoctor: returns distinct patients who visited doctor, sorted and mapped")
    void showAllPatientsWhoVisitedDoctor_sortedAndMapped() {
        long doctorId = 77L;

        Doctor gp = doctor(300L, "Mila", "Koleva", "Dermatology", true);

        Patient p1 = patient(5L, "Zara", "Vane", LocalDate.of(1999, 9, 9), gp);
        Patient p2 = patient(4L, "Ivan", "Petrov", LocalDate.of(1995, 2, 2), gp);
        Patient p3 = patient(6L, "Ivan", "Atanasov", LocalDate.of(1996, 3, 3), gp);

        // Repo returns in weird order
        when(patientRepository.findDistinctByVisitedDoctor(doctorId))
                .thenReturn(List.of(p1, p2, p3));

        Set<PatientDataWithDoctorDtoResponse> out = service.showAllPatientsWhoVisitedDoctor(doctorId);

        // verify repo call
        verify(patientRepository).findDistinctByVisitedDoctor(doctorId);

        // expected sorting:
        // by firstName asc:
        //   "Ivan" (p3, p2) then "Zara" (p1)
        // within "Ivan": lastName asc => "Atanasov" (p3) before "Petrov" (p2)
        // all unique IDs considered in tiebreaker if names match
        List<Long> sortedIds = out.stream().map(dto -> dto.getPatientData().getId()).toList();
        assertThat(sortedIds).containsExactly(6L, 4L, 5L);

        // sanity-check mapping for one dto
        PatientDataWithDoctorDtoResponse dtoIvanA = out.iterator().next();
        assertThat(dtoIvanA.getPatientData().getFirstName()).isEqualTo("Ivan");
        assertThat(dtoIvanA.getDoctorData().getFirstName()).isEqualTo("Mila");
        assertThat(dtoIvanA.getDoctorData().getSpecializationType()).isEqualTo("Dermatology");
    }

    // ---------- totalCountPatientsWhoVisitedDoctor ----------
    @Test
    @DisplayName("totalCountPatientsWhoVisitedDoctor delegates to repository.countDistinctByVisitedDoctor")
    void totalCountPatientsWhoVisitedDoctor_delegates() {
        when(patientRepository.countDistinctByVisitedDoctor(55L)).thenReturn(12);

        int result = service.totalCountPatientsWhoVisitedDoctor(55L);

        verify(patientRepository).countDistinctByVisitedDoctor(55L);
        assertThat(result).isEqualTo(12);
    }

    // ---------- showAllPatientsWithResultDiagnosis ----------
    @Test
    @DisplayName("showAllPatientsWithResultDiagnosis: queries by diagnosis result, sorts, maps")
    void showAllPatientsWithResultDiagnosis_sortedAndMapped() {
        Patient px = patient(1L, "Ana", "Bee", LocalDate.of(2001, 1, 1), null);
        Patient py = patient(2L, "Ana", "Aaa", LocalDate.of(2002, 2, 2), null);

        when(patientRepository.findDistinctByDiagnosisResult("Flu"))
                .thenReturn(List.of(px, py));

        Set<PatientDataWithDoctorDtoResponse> out = service.showAllPatientsWithResultDiagnosis("Flu");

        verify(patientRepository).findDistinctByDiagnosisResult("Flu");

        // sort by firstName asc ("Ana"/"Ana"), lastName asc ("Aaa" then "Bee"), then id
        List<Long> sortedIds = out.stream().map(dto -> dto.getPatientData().getId()).toList();
        assertThat(sortedIds).containsExactly(2L, 1L);

        // mapped fields should include first/last/birthDate
        PatientDataWithDoctorDtoResponse first = out.iterator().next();
        assertThat(first.getPatientData().getFirstName()).isEqualTo("Ana");
        assertThat(first.getPatientData().getLastName()).isEqualTo("Aaa");
        assertThat(first.getDoctorData()).isNull(); // no GP given above
    }

    // ---------- totalCountPatientsWithResultDiagnosis ----------
    @Test
    @DisplayName("totalCountPatientsWithResultDiagnosis delegates to repository.countDistinctByDiagnosisResult")
    void totalCountPatientsWithResultDiagnosis_delegates() {
        when(patientRepository.countDistinctByDiagnosisResult("Covid")).thenReturn(3);

        int result = service.totalCountPatientsWithResultDiagnosis("Covid");

        verify(patientRepository).countDistinctByDiagnosisResult("Covid");
        assertThat(result).isEqualTo(3);
    }

    // ---------- showAllPatientsWithAllergy ----------
    @Test
    @DisplayName("showAllPatientsWithAllergy: queries by allergen, sorts, maps with GP info")
    void showAllPatientsWithAllergy_sortedAndMapped() {
        Doctor gp = doctor(900L, "Simeon", "Kolev", "Allergology", true);

        Patient a = patient(10L, "Maya", "Zed", LocalDate.of(1991, 4, 4), gp);
        Patient b = patient(11L, "Maya", "Ace", LocalDate.of(1992, 5, 5), gp);

        when(patientRepository.findDistinctByAllergen("pollen"))
                .thenReturn(List.of(a, b));

        Set<PatientDataWithDoctorDtoResponse> out = service.showAllPatientsWithAllergy("pollen");

        verify(patientRepository).findDistinctByAllergen("pollen");

        // Sorting:
        // firstName: "Maya"/"Maya"
        // lastName: "Ace" < "Zed"
        // so b(id=11) before a(id=10)
        List<Long> ids = out.stream().map(dto -> dto.getPatientData().getId()).toList();
        assertThat(ids).containsExactly(11L, 10L);

        // verify doctor mapping is present and correct
        PatientDataWithDoctorDtoResponse first = out.iterator().next();
        assertThat(first.getDoctorData()).isNotNull();
        assertThat(first.getDoctorData().getId()).isEqualTo(900L);
        assertThat(first.getDoctorData().getFirstName()).isEqualTo("Simeon");
        assertThat(first.getDoctorData().getLastName()).isEqualTo("Kolev");
        assertThat(first.getDoctorData().getSpecializationType()).isEqualTo("Allergology");
        assertThat(first.getDoctorData().isGp()).isTrue();
    }
}

