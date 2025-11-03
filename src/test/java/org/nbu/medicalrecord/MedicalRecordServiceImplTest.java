package org.nbu.medicalrecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.request.MedicalRecordDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicalRecordDtoResponse;
import org.nbu.medicalrecord.entities.MedicalRecord;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.entities.User;
import org.nbu.medicalrecord.entities.Visit;
import org.nbu.medicalrecord.repositories.MedicalRecordRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.impl.MedicalRecordServiceImpl;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceImplTest {

    @Mock
    private MedicalRecordRepository recordRepo;

    @Mock
    private PatientRepository patientRepo;

    @InjectMocks
    private MedicalRecordServiceImpl service;

    private Patient buildPatient(long id, String first, String last, LocalDate birthDate) {
        Patient p = new Patient();
        p.setId(id);

        User u = new User();
        u.setFirstName(first);
        u.setLastName(last);
        p.setUser(u);

        p.setBirthDate(birthDate);
        return p;
    }

    @Nested
    @DisplayName("createNewMedicalRecord")
    class CreateNewMedicalRecordTests {

        @Test
        @DisplayName("throws when patient not found")
        void patientNotFound_throws() {
            MedicalRecordDtoRequest req = new MedicalRecordDtoRequest();
            req.setPatientId(10L);

            when(patientRepo.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createNewMedicalRecord(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Patient not found");

            verifyNoInteractions(recordRepo);
        }

        @Test
        @DisplayName("throws when medical record already exists for this patient")
        void recordAlreadyExists_throws() {
            MedicalRecordDtoRequest req = new MedicalRecordDtoRequest();
            req.setPatientId(5L);

            Patient patient = buildPatient(5L, "Ana", "Ivanova", LocalDate.of(1995, 5, 15));

            when(patientRepo.findById(5L)).thenReturn(Optional.of(patient));
            when(recordRepo.existsByPatient_Id(5L)).thenReturn(true);

            assertThatThrownBy(() -> service.createNewMedicalRecord(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already exists");

            verify(recordRepo, never()).save(any());
        }

        @Test
        @DisplayName("creates new record, initializes visits empty, saves, and returns mapped DTO")
        void createRecordSuccessfully() {
            MedicalRecordDtoRequest req = new MedicalRecordDtoRequest();
            req.setPatientId(7L);

            Patient patient = buildPatient(7L, "Ivan", "Petrov", LocalDate.of(1990, 1, 2));

            when(patientRepo.findById(7L)).thenReturn(Optional.of(patient));
            when(recordRepo.existsByPatient_Id(7L)).thenReturn(false);

            when(recordRepo.save(any(MedicalRecord.class))).thenAnswer(inv -> {
                MedicalRecord rec = inv.getArgument(0);
                rec.setId(123L);
                return rec;
            });

            MedicalRecordDtoResponse out = service.createNewMedicalRecord(req);

            ArgumentCaptor<MedicalRecord> captor = ArgumentCaptor.forClass(MedicalRecord.class);
            verify(recordRepo).save(captor.capture());
            MedicalRecord saved = captor.getValue();

            assertThat(saved.getPatient()).isSameAs(patient);
            assertThat(saved.getVisits()).isNotNull();
            assertThat(saved.getVisits()).isEmpty();

            assertThat(out.getId()).isEqualTo(123L);
            assertThat(out.getPatientId()).isEqualTo(7L);
            assertThat(out.getFirstName()).isEqualTo("Ivan");
            assertThat(out.getLastName()).isEqualTo("Petrov");
            assertThat(out.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 2));
            assertThat(out.getVisitsCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("showMedicalRecord")
    class ShowMedicalRecordTests {

        @Test
        @DisplayName("returns DTO when record exists (visit count included)")
        void recordFound() {
            Patient patient = buildPatient(3L, "Maya", "Dimitrova", LocalDate.of(2000, 3, 20));

            MedicalRecord rec = new MedicalRecord();
            rec.setId(44L);
            rec.setPatient(patient);
            rec.setVisits(new HashSet<>(Arrays.asList(new Visit(), new Visit())));

            when(recordRepo.findByPatient_Id(3L)).thenReturn(Optional.of(rec));

            MedicalRecordDtoResponse dto = service.showMedicalRecord(3L);

            assertThat(dto.getId()).isEqualTo(44L);
            assertThat(dto.getPatientId()).isEqualTo(3L);
            assertThat(dto.getFirstName()).isEqualTo("Maya");
            assertThat(dto.getLastName()).isEqualTo("Dimitrova");
            assertThat(dto.getBirthDate()).isEqualTo(LocalDate.of(2000, 3, 20));
            assertThat(dto.getVisitsCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("throws when record not found for that patient")
        void recordMissing_throws() {
            when(recordRepo.findByPatient_Id(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.showMedicalRecord(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Medical record not found");
        }
    }

    @Test
    @DisplayName("showAllMedicalRecords maps all records to DTOs and returns a LinkedHashSet")
    void showAllMedicalRecords_maps() {
        Patient p1 = buildPatient(1L, "Ana", "Zed", LocalDate.of(1999, 2, 2));
        Patient p2 = buildPatient(2L, "Boris", "Alpha", LocalDate.of(1988, 7, 7));

        MedicalRecord r1 = new MedicalRecord();
        r1.setId(10L);
        r1.setPatient(p1);
        r1.setVisits(new HashSet<>(List.of(new Visit())));

        MedicalRecord r2 = new MedicalRecord();
        r2.setId(11L);
        r2.setPatient(p2);
        r2.setVisits(new HashSet<>());

        when(recordRepo.findAll()).thenReturn(List.of(r1, r2));

        Set<MedicalRecordDtoResponse> out = service.showAllMedicalRecords();

        assertThat(out).hasSize(2);

        Optional<MedicalRecordDtoResponse> r1dtoOpt = out.stream()
                .filter(d -> d.getId() == 10L)
                .findFirst();
        assertThat(r1dtoOpt).isPresent();
        MedicalRecordDtoResponse r1dto = r1dtoOpt.get();
        assertThat(r1dto.getPatientId()).isEqualTo(1L);
        assertThat(r1dto.getFirstName()).isEqualTo("Ana");
        assertThat(r1dto.getLastName()).isEqualTo("Zed");
        assertThat(r1dto.getVisitsCount()).isEqualTo(1);

        Optional<MedicalRecordDtoResponse> r2dtoOpt = out.stream()
                .filter(d -> d.getId() == 11L)
                .findFirst();
        assertThat(r2dtoOpt).isPresent();
        MedicalRecordDtoResponse r2dto = r2dtoOpt.get();
        assertThat(r2dto.getPatientId()).isEqualTo(2L);
        assertThat(r2dto.getFirstName()).isEqualTo("Boris");
        assertThat(r2dto.getLastName()).isEqualTo("Alpha");
        assertThat(r2dto.getVisitsCount()).isEqualTo(0);
    }

    @Nested
    @DisplayName("deleteMedicalRecord")
    class DeleteMedicalRecordTests {

        @Test
        @DisplayName("throws when record does not exist")
        void deleteMissing_throws() {
            when(recordRepo.existsById(500L)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteMedicalRecord(500L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");

            verify(recordRepo, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("deletes when record exists")
        void deleteSuccessfully() {
            when(recordRepo.existsById(42L)).thenReturn(true);

            service.deleteMedicalRecord(42L);

            verify(recordRepo).deleteById(42L);
        }
    }
}
