package org.nbu.medicalrecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.request.SickLeaveDtoRequest;
import org.nbu.medicalrecord.dtos.response.SickLeaveDtoResponse;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.entities.SickLeave;
import org.nbu.medicalrecord.entities.User;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.repositories.SickLeaveRepository;
import org.nbu.medicalrecord.services.impl.SickLeaveServiceImpl;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SickLeaveServiceImplTest {

    @Mock
    private SickLeaveRepository sickLeaveRepo;

    @Mock
    private DoctorRepository doctorRepo;

    @Mock
    private PatientRepository patientRepo;

    @InjectMocks
    private SickLeaveServiceImpl service;

    private Doctor buildDoctor(long id, String first, String last) {
        Doctor d = new Doctor();
        d.setId(id);
        User u = new User();
        u.setFirstName(first);
        u.setLastName(last);
        d.setUser(u);
        return d;
    }

    private Patient buildPatient(long id, String first, String last) {
        Patient p = new Patient();
        p.setId(id);
        User u = new User();
        u.setFirstName(first);
        u.setLastName(last);
        p.setUser(u);
        return p;
    }

    // ---------- createSickLeave ----------
    @Nested
    @DisplayName("createSickLeave")
    class CreateSickLeaveTests {

        @Test
        @DisplayName("throws if fromDate is after toDate")
        void invalidDateRange_throws() {
            SickLeaveDtoRequest req = new SickLeaveDtoRequest();
            req.setDoctorId(1L);
            req.setPatientId(2L);
            req.setFromDate(LocalDate.of(2025, 10, 10));
            req.setToDate(LocalDate.of(2025, 10, 9)); // invalid
            req.setReason("Flu");

            assertThatThrownBy(() -> service.createSickLeave(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("fromDate")
                    .hasMessageContaining("toDate");

            verifyNoInteractions(doctorRepo, patientRepo, sickLeaveRepo);
        }

        @Test
        @DisplayName("throws if toDate is more than 1 year in the past")
        void tooFarInPast_throws() {
            SickLeaveDtoRequest req = new SickLeaveDtoRequest();
            req.setDoctorId(1L);
            req.setPatientId(2L);
            req.setFromDate(LocalDate.now().minusYears(2));
            req.setToDate(LocalDate.now().minusYears(2));
            req.setReason("Old stuff");

            assertThatThrownBy(() -> service.createSickLeave(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("too far in the past");

            verifyNoInteractions(doctorRepo, patientRepo, sickLeaveRepo);
        }

        @Test
        @DisplayName("throws if doctor not found")
        void missingDoctor_throws() {
            SickLeaveDtoRequest req = new SickLeaveDtoRequest();
            req.setDoctorId(10L);
            req.setPatientId(20L);
            req.setFromDate(LocalDate.now());
            req.setToDate(LocalDate.now().plusDays(2));
            req.setReason("Flu");

            when(doctorRepo.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createSickLeave(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Doctor not found");

            verify(doctorRepo).findById(10L);
            verifyNoInteractions(patientRepo, sickLeaveRepo);
        }

        @Test
        @DisplayName("throws if patient not found")
        void missingPatient_throws() {
            SickLeaveDtoRequest req = new SickLeaveDtoRequest();
            req.setDoctorId(10L);
            req.setPatientId(20L);
            req.setFromDate(LocalDate.now());
            req.setToDate(LocalDate.now().plusDays(3));
            req.setReason("Migraine");

            Doctor d = buildDoctor(10L, "Ana", "Dimitrova");
            when(doctorRepo.findById(10L)).thenReturn(Optional.of(d));
            when(patientRepo.findById(20L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createSickLeave(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Patient not found");

            verify(patientRepo).findById(20L);
            verify(sickLeaveRepo, never()).save(any());
        }

        @Test
        @DisplayName("creates SickLeave, saves it, and returns mapped DTO (includes doctor/patient names and duration days)")
        void createSickLeaveSuccessfully() {
            SickLeaveDtoRequest req = new SickLeaveDtoRequest();
            req.setDoctorId(10L);
            req.setPatientId(20L);
            req.setFromDate(LocalDate.of(2025, 10, 1));
            req.setToDate(LocalDate.of(2025, 10, 4));
            req.setReason("Back pain");

            Doctor d = buildDoctor(10L, "Ana", "Dimitrova");
            Patient p = buildPatient(20L, "Ivan", "Petrov");

            when(doctorRepo.findById(10L)).thenReturn(Optional.of(d));
            when(patientRepo.findById(20L)).thenReturn(Optional.of(p));

            // simulate JPA ID assignment on save
            when(sickLeaveRepo.save(any(SickLeave.class))).thenAnswer(inv -> {
                SickLeave sl = inv.getArgument(0);
                sl.setId(123L);
                return sl;
            });

            SickLeaveDtoResponse out = service.createSickLeave(req);

            // verify the SickLeave we attempted to persist
            ArgumentCaptor<SickLeave> captor = ArgumentCaptor.forClass(SickLeave.class);
            verify(sickLeaveRepo).save(captor.capture());
            SickLeave saved = captor.getValue();

            assertThat(saved.getDoctor()).isSameAs(d);
            assertThat(saved.getPatient()).isSameAs(p);
            assertThat(saved.getFromDate()).isEqualTo(LocalDate.of(2025, 10, 1));
            assertThat(saved.getToDate()).isEqualTo(LocalDate.of(2025, 10, 4));
            assertThat(saved.getReason()).isEqualTo("Back pain");

            // verify DTO correctness
            assertThat(out.getId()).isEqualTo(123L);
            assertThat(out.getDoctorId()).isEqualTo(10L);
            assertThat(out.getDoctorName()).isEqualTo("Ana Dimitrova");
            assertThat(out.getPatientId()).isEqualTo(20L);
            assertThat(out.getPatientName()).isEqualTo("Ivan Petrov");
            assertThat(out.getFromDate()).isEqualTo(LocalDate.of(2025, 10, 1));
            assertThat(out.getToDate()).isEqualTo(LocalDate.of(2025, 10, 4));
            assertThat(out.getReason()).isEqualTo("Back pain");

            // inclusive day count: Oct 1 to Oct 4 = 4 days
            long expectedDays = ChronoUnit.DAYS.between(
                    LocalDate.of(2025, 10, 1),
                    LocalDate.of(2025, 10, 4)
            ) + 1;
            assertThat(out.getTotalDays()).isEqualTo((int) expectedDays);
        }
    }

    // ---------- showAllSickLeavesByDoctor ----------
    @Test
    @DisplayName("showAllSickLeavesByDoctor: maps SickLeave entities to DTOs and preserves iteration order from repo")
    void showAllSickLeavesByDoctor_maps() {
        Doctor d = buildDoctor(5L, "Mila", "Koleva");
        Patient p = buildPatient(7L, "Boris", "Ivanov");

        SickLeave sl1 = new SickLeave();
        sl1.setId(1L);
        sl1.setDoctor(d);
        sl1.setPatient(p);
        sl1.setFromDate(LocalDate.of(2025, 9, 10));
        sl1.setToDate(LocalDate.of(2025, 9, 11));
        sl1.setReason("Cold");

        SickLeave sl2 = new SickLeave();
        sl2.setId(2L);
        sl2.setDoctor(d);
        sl2.setPatient(p);
        sl2.setFromDate(LocalDate.of(2025, 9, 1));
        sl2.setToDate(LocalDate.of(2025, 9, 3));
        sl2.setReason("Flu");

        // service expects repo to already sort descending by fromDate
        when(sickLeaveRepo.findByDoctor_IdOrderByFromDateDesc(5L))
                .thenReturn(List.of(sl1, sl2));

        Set<SickLeaveDtoResponse> out = service.showAllSickLeavesByDoctor(5L);

        verify(sickLeaveRepo).findByDoctor_IdOrderByFromDateDesc(5L);

        assertThat(out).hasSize(2);

        // verify first element data
        SickLeaveDtoResponse first = out.iterator().next();
        assertThat(first.getId()).isEqualTo(1L);
        assertThat(first.getDoctorName()).isEqualTo("Mila Koleva");
        assertThat(first.getPatientName()).isEqualTo("Boris Ivanov");
        assertThat(first.getReason()).isEqualTo("Cold");
        assertThat(first.getTotalDays()).isEqualTo(2); // 10th-11th inclusive
    }

    // ---------- showAllSickLeavesForPatient ----------
    @Test
    @DisplayName("showAllSickLeavesForPatient: maps SickLeave entities to DTOs and preserves repo order")
    void showAllSickLeavesForPatient_maps() {
        Doctor d = buildDoctor(5L, "Ana", "Dimitrova");
        Patient p = buildPatient(7L, "Ivan", "Petrov");

        SickLeave slA = new SickLeave();
        slA.setId(10L);
        slA.setDoctor(d);
        slA.setPatient(p);
        slA.setFromDate(LocalDate.of(2025, 8, 20));
        slA.setToDate(LocalDate.of(2025, 8, 21));
        slA.setReason("Injury");

        SickLeave slB = new SickLeave();
        slB.setId(11L);
        slB.setDoctor(d);
        slB.setPatient(p);
        slB.setFromDate(LocalDate.of(2025, 8, 1));
        slB.setToDate(LocalDate.of(2025, 8, 5));
        slB.setReason("Flu");

        when(sickLeaveRepo.findByPatient_IdOrderByFromDateDesc(7L))
                .thenReturn(List.of(slA, slB));

        Set<SickLeaveDtoResponse> out = service.showAllSickLeavesForPatient(7L);

        verify(sickLeaveRepo).findByPatient_IdOrderByFromDateDesc(7L);

        assertThat(out).hasSize(2);

        SickLeaveDtoResponse first = out.iterator().next();
        assertThat(first.getId()).isEqualTo(10L);
        assertThat(first.getDoctorName()).isEqualTo("Ana Dimitrova");
        assertThat(first.getPatientName()).isEqualTo("Ivan Petrov");
        assertThat(first.getTotalDays()).isEqualTo(2); // 20th-21st inclusive
    }

    // ---------- showMonthWithMostSickLeaves ----------
    @Nested
    @DisplayName("showMonthWithMostSickLeaves")
    class ShowMonthWithMostSickLeavesTests {

        @Test
        @DisplayName("returns null when repository returns empty list")
        void returnsNullOnEmpty() {
            when(sickLeaveRepo.countByMonthDesc()).thenReturn(List.of());

            Month result = service.showMonthWithMostSickLeaves();

            verify(sickLeaveRepo).countByMonthDesc();
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns Month.of(m) from first projection row")
        void returnsTopMonth() {
            // We need to mock SickLeaveRepository.MonthCount-like projection
            SickLeaveRepository.MonthCount mc = new SickLeaveRepository.MonthCount() {
                @Override
                public Integer getM() {
                    return 9; // September
                }
                @Override
                public long getC() {
                    return 42L;
                }
            };

            when(sickLeaveRepo.countByMonthDesc()).thenReturn(List.of(mc));

            Month result = service.showMonthWithMostSickLeaves();

            verify(sickLeaveRepo).countByMonthDesc();
            assertThat(result).isEqualTo(Month.SEPTEMBER);
        }
    }
}

