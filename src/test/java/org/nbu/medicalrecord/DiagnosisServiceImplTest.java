package org.nbu.medicalrecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.request.DiagnosisDtoRequest;
import org.nbu.medicalrecord.dtos.request.MedicationDtoRequest;
import org.nbu.medicalrecord.dtos.response.DiagnosisDtoResponse;
import org.nbu.medicalrecord.entities.*;
import org.nbu.medicalrecord.repositories.DiagnosisRepository;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.MedicineRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.impl.DiagnosisServiceImpl;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosisServiceImplTest {

    @Mock
    private DiagnosisRepository diagnosisRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @InjectMocks
    private DiagnosisServiceImpl service;

    @Nested
    class CreateDiagnosis {

        @Test
        @DisplayName("creates diagnosis with medication when doctor/patient exist and all medicineIds are valid")
        void createDiagnosisWithMedicationSuccessfully() {
            // doctor
            Doctor doctor = new Doctor();
            doctor.setId(2L);
            User du = new User(); du.setFirstName("Ana"); du.setLastName("Dimitrova");
            doctor.setUser(du);

            // patient
            Patient patient = new Patient();
            patient.setId(3L);
            User pu = new User(); pu.setFirstName("Ivan"); pu.setLastName("Petrov");
            patient.setUser(pu);
            patient.setBirthDate(LocalDate.of(1990, 1, 1));

            when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
            when(patientRepository.findById(3L)).thenReturn(Optional.of(patient));

            // medicines returned for IDs 10, 20
            Medicine m1 = new Medicine(); m1.setId(10L); m1.setName("Ibuprofen"); m1.setMg(200);
            Medicine m2 = new Medicine(); m2.setId(20L); m2.setName("Aspirin");  m2.setMg(100);
            when(medicineRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(m1, m2));

            // request
            MedicationDtoRequest medReq = new MedicationDtoRequest();
            medReq.setMedicineIds(List.of(10L, 20L));
            medReq.setPrescription("1 tab twice daily");

            DiagnosisDtoRequest req = new DiagnosisDtoRequest();
            req.setDoctorId(2L);
            req.setPatientId(3L);
            req.setComplaints("Headache");
            req.setMedicalHistory("None");
            req.setDiagnosisResult("Migraine");
            req.setRequiredTests("None");
            req.setMedication(medReq);

            doAnswer(inv -> {
                Diagnosis d = inv.getArgument(0);
                d.setId(99L);
                return d;
            }).when(diagnosisRepository).save(any(Diagnosis.class));

            DiagnosisDtoResponse out = service.createDiagnosis(req);

            ArgumentCaptor<Diagnosis> captor = ArgumentCaptor.forClass(Diagnosis.class);
            verify(diagnosisRepository).save(captor.capture());
            Diagnosis saved = captor.getValue();

            assertThat(saved.getDoctor().getId()).isEqualTo(2L);
            assertThat(saved.getPatient().getId()).isEqualTo(3L);
            assertThat(saved.getDiagnosisResult()).isEqualTo("Migraine");
            assertThat(saved.getMedication()).isNotNull();
            assertThat(saved.getMedication().getMedicines()).extracting(Medicine::getId)
                    .containsExactlyInAnyOrder(10L, 20L);
            assertThat(saved.getMedication().getPrescription()).isEqualTo("1 tab twice daily");

            assertThat(out.getId()).isEqualTo(99L);
            assertThat(out.getDoctorName()).isEqualTo("Ana Dimitrova");
            assertThat(out.getPatientName()).isEqualTo("Ivan Petrov");
            assertThat(out.getDiagnosisResult()).isEqualTo("Migraine");
            assertThat(out.getMedication()).isNotNull();
            assertThat(out.getMedication().getPrescription()).isEqualTo("1 tab twice daily");

            List<String> medNamesOrder = out.getMedication().getMedicines().stream()
                    .map(m -> m.getName() + ":" + m.getMg())
                    .toList();
            assertThat(medNamesOrder).containsExactly("Aspirin:100", "Ibuprofen:200");
        }

        @Test
        @DisplayName("creates diagnosis with NO medication when medication request is null")
        void createDiagnosisWithoutMedicationSuccessfully() {
            Doctor doctor = new Doctor(); doctor.setId(5L);
            User du = new User(); du.setFirstName("Doc"); du.setLastName("Tor");
            doctor.setUser(du);
            Patient patient = new Patient(); patient.setId(6L);
            User pu = new User(); pu.setFirstName("Pat"); pu.setLastName("Ient");
            patient.setUser(pu);

            when(doctorRepository.findById(5L)).thenReturn(Optional.of(doctor));
            when(patientRepository.findById(6L)).thenReturn(Optional.of(patient));

            DiagnosisDtoRequest req = new DiagnosisDtoRequest();
            req.setDoctorId(5L);
            req.setPatientId(6L);
            req.setDiagnosisResult("Cold");
            req.setMedication(null);

            doAnswer(inv -> {
                Diagnosis d = inv.getArgument(0);
                d.setId(1L);
                return d;
            }).when(diagnosisRepository).save(any(Diagnosis.class));

            DiagnosisDtoResponse out = service.createDiagnosis(req);

            verify(medicineRepository, never()).findAllById(any());
            assertThat(out.getMedication()).isNull();
            assertThat(out.getDiagnosisResult()).isEqualTo("Cold");
        }

        @Test
        @DisplayName("throws when doctor is not found")
        void doctorNotFound_throws() {
            when(doctorRepository.findById(123L)).thenReturn(Optional.empty());

            DiagnosisDtoRequest req = new DiagnosisDtoRequest();
            req.setDoctorId(123L);
            req.setPatientId(1L);

            assertThatThrownBy(() -> service.createDiagnosis(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Doctor not found");
            verifyNoInteractions(patientRepository, medicineRepository, diagnosisRepository);
        }

        @Test
        @DisplayName("throws when patient is not found")
        void patientNotFound_throws() {
            Doctor doctor = new Doctor(); doctor.setId(1L);
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
            when(patientRepository.findById(2L)).thenReturn(Optional.empty());

            DiagnosisDtoRequest req = new DiagnosisDtoRequest();
            req.setDoctorId(1L);
            req.setPatientId(2L);

            assertThatThrownBy(() -> service.createDiagnosis(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Patient not found");
            verify(medicineRepository, never()).findAllById(any());
            verify(diagnosisRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws when some medicineIds do not exist")
        void medicinesNotFound_throws() {
            Doctor doctor = new Doctor(); doctor.setId(10L);
            when(doctorRepository.findById(10L)).thenReturn(Optional.of(doctor));

            Patient patient = new Patient(); patient.setId(11L);
            when(patientRepository.findById(11L)).thenReturn(Optional.of(patient));

            MedicationDtoRequest medReq = new MedicationDtoRequest();
            medReq.setMedicineIds(List.of(1L, 2L)); // two requested
            medReq.setPrescription("rx");

            DiagnosisDtoRequest req = new DiagnosisDtoRequest();
            req.setDoctorId(10L);
            req.setPatientId(11L);
            req.setMedication(medReq);

            // repository returns only one -> mismatch
            Medicine m = new Medicine(); m.setId(1L); m.setName("X"); m.setMg(10);
            when(medicineRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(m));

            assertThatThrownBy(() -> service.createDiagnosis(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("medicineIds do not exist");
            verify(diagnosisRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("showAllDiagnosisForPatientId: maps to DTO and returns a linked set")
    void showAllDiagnosisForPatient() {
        Diagnosis d1 = new Diagnosis(); d1.setId(1L);
        Diagnosis d2 = new Diagnosis(); d2.setId(2L);

        // doctor/patient names for mapping
        Doctor doc = new Doctor();
        User du = new User(); du.setFirstName("Ana"); du.setLastName("D");
        doc.setUser(du);
        d1.setDoctor(doc); d2.setDoctor(doc);

        Patient pat = new Patient();
        User pu = new User(); pu.setFirstName("Ivan"); pu.setLastName("P");
        pat.setUser(pu);
        d1.setPatient(pat); d2.setPatient(pat);

        Allergy a = new Allergy(); a.setAllergen("latex");
        Allergy b = new Allergy(); b.setAllergen("pollen");
        d1.setAllergies(new HashSet<>(Set.of(b, a)));

        when(diagnosisRepository.findByPatient_Id(77L)).thenReturn(List.of(d1, d2));

        Set<DiagnosisDtoResponse> out = service.showAllDiagnosisForPatientId(77L);
        assertThat(out).hasSize(2);
        assertThat(out.stream().filter(dto -> Objects.equals(dto.getId(), 1L)).findFirst().orElseThrow()
                .getAllergies()).containsExactly("latex", "pollen");
        assertThat(out).allMatch(dto -> "Ana D".equals(dto.getDoctorName()));
        assertThat(out).allMatch(dto -> "Ivan P".equals(dto.getPatientName()));
    }

    @Test
    @DisplayName("showAllDiagnosisByDoctorId: maps to DTO for each entity")
    void showAllDiagnosisByDoctor() {
        Diagnosis d1 = new Diagnosis(); d1.setId(10L);
        Diagnosis d2 = new Diagnosis(); d2.setId(11L);

        Doctor doc = new Doctor();
        User du = new User(); du.setFirstName("Doc"); du.setLastName("Tor");
        doc.setUser(du);
        d1.setDoctor(doc); d2.setDoctor(doc);

        Patient p = new Patient();
        User pu = new User(); pu.setFirstName("Pat"); pu.setLastName("Ient");
        p.setUser(pu);
        d1.setPatient(p); d2.setPatient(p);

        when(diagnosisRepository.findByDoctor_Id(5L)).thenReturn(List.of(d1, d2));

        Set<DiagnosisDtoResponse> out = service.showAllDiagnosisByDoctorId(5L);
        assertThat(out).hasSize(2);
        assertThat(out).allMatch(dto -> "Doc Tor".equals(dto.getDoctorName()));
        assertThat(out).allMatch(dto -> "Pat Ient".equals(dto.getPatientName()));
    }

    @Nested
    class MostFrequent {

        @Test
        @DisplayName("returns empty set when there are no counts")
        void emptyCounts() {
            when(diagnosisRepository.countByDiagnosisResultDesc()).thenReturn(List.of());
            assertThat(service.showMostFrequentDiagnosisResult()).isEmpty();
            verify(diagnosisRepository, never()).findAll();
        }

        @Test
        @DisplayName("returns one sample per top result (handles ties)")
        void tieTopResults() {
            DiagnosisRepository.DiagnosisResultCount c1 = new DiagnosisRepository.DiagnosisResultCount() {
                public String diagnosisResult() { return "Flu"; }
                public long cnt() { return 5L; }
            };
            DiagnosisRepository.DiagnosisResultCount c2 = new DiagnosisRepository.DiagnosisResultCount() {
                public String diagnosisResult() { return "Cold"; }
                public long cnt() { return 5L; }
            };
            DiagnosisRepository.DiagnosisResultCount c3 = new DiagnosisRepository.DiagnosisResultCount() {
                public String diagnosisResult() { return "Allergy"; }
                public long cnt() { return 2L; }
            };
            when(diagnosisRepository.countByDiagnosisResultDesc()).thenReturn(List.of(c1, c2, c3));

            Diagnosis dFlu1 = new Diagnosis(); dFlu1.setId(1L); dFlu1.setDiagnosisResult("Flu");
            Diagnosis dFlu2 = new Diagnosis(); dFlu2.setId(2L); dFlu2.setDiagnosisResult("Flu");
            Diagnosis dCold = new Diagnosis(); dCold.setId(3L); dCold.setDiagnosisResult("Cold");
            Diagnosis dOther = new Diagnosis(); dOther.setId(4L); dOther.setDiagnosisResult("Allergy");

            when(diagnosisRepository.findAll()).thenReturn(List.of(dFlu1, dFlu2, dCold, dOther));

            Set<DiagnosisDtoResponse> out = service.showMostFrequentDiagnosisResult();
            assertThat(out).hasSize(2);
            assertThat(out).extracting(DiagnosisDtoResponse::getDiagnosisResult)
                    .containsExactlyInAnyOrder("Flu", "Cold");
            assertThat(out.stream().map(DiagnosisDtoResponse::getDiagnosisResult).distinct().count()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("deleteDiagnosis: throws when missing; otherwise deletes by id")
    void deleteDiagnosis_behaviour() {
        when(diagnosisRepository.existsById(100L)).thenReturn(false);
        assertThatThrownBy(() -> service.deleteDiagnosis(100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");

        when(diagnosisRepository.existsById(100L)).thenReturn(true);
        service.deleteDiagnosis(100L);
        verify(diagnosisRepository).deleteById(100L);
    }
}
