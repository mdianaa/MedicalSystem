package org.nbu.medicalrecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.request.HealthInsuranceDtoRequest;
import org.nbu.medicalrecord.dtos.response.HealthInsuranceDtoResponse;
import org.nbu.medicalrecord.entities.HealthInsurance;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.repositories.HealthInsuranceRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.impl.HealthInsuranceServiceImpl;

import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthInsuranceServiceImplTest {

    @Mock
    private HealthInsuranceRepository repo;

    @Mock
    private PatientRepository patientRepo;

    @InjectMocks
    private HealthInsuranceServiceImpl service;

    @Nested
    @DisplayName("createNewHealthInsurance")
    class CreateNew {

        @Test
        @DisplayName("throws when patient not found")
        void patientNotFound() {
            HealthInsuranceDtoRequest req = new HealthInsuranceDtoRequest();
            req.setPatientId(9L);
            req.setMonth(Month.MARCH);
            req.setYear(2025);
            when(patientRepo.findById(9L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createNewHealthInsurance(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Patient not found");

            verifyNoInteractions(repo);
        }

        @Test
        @DisplayName("throws when entry already exists for month/year")
        void duplicateEntry() {
            Patient p = new Patient(); p.setId(3L);
            when(patientRepo.findById(3L)).thenReturn(Optional.of(p));
            when(repo.existsByPatient_IdAndMonthAndYear(3L, Month.APRIL, 2025)).thenReturn(true);

            HealthInsuranceDtoRequest req = new HealthInsuranceDtoRequest();
            req.setPatientId(3L);
            req.setMonth(Month.APRIL);
            req.setYear(2025);

            assertThatThrownBy(() -> service.createNewHealthInsurance(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already exists");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("saves new HealthInsurance and returns mapped DTO")
        void savesAndMaps() {
            Patient p = new Patient(); p.setId(3L);
            when(patientRepo.findById(3L)).thenReturn(Optional.of(p));
            when(repo.existsByPatient_IdAndMonthAndYear(3L, Month.APRIL, 2025)).thenReturn(false);

            when(repo.save(any(HealthInsurance.class))).thenAnswer(inv -> {
                HealthInsurance hi = inv.getArgument(0);
                hi.setId(11L);
                return hi;
            });

            HealthInsuranceDtoRequest req = new HealthInsuranceDtoRequest();
            req.setPatientId(3L);
            req.setMonth(Month.APRIL);
            req.setYear(2025);
            req.setPaid(false);

            HealthInsuranceDtoResponse out = service.createNewHealthInsurance(req);

            ArgumentCaptor<HealthInsurance> cap = ArgumentCaptor.forClass(HealthInsurance.class);
            verify(repo).save(cap.capture());
            HealthInsurance saved = cap.getValue();

            assertThat(saved.getPatient().getId()).isEqualTo(3L);
            assertThat(saved.getMonth()).isEqualTo(Month.APRIL);
            assertThat(saved.getYear()).isEqualTo(2025);
            assertThat(saved.isPaid()).isFalse();

            assertThat(out.getId()).isEqualTo(11L);
            assertThat(out.getPatientId()).isEqualTo(3L);
            assertThat(out.getMonth()).isEqualTo(Month.APRIL);
            assertThat(out.getYear()).isEqualTo(2025);
            assertThat(out.isPaid()).isFalse();
        }
    }

    @Nested
    @DisplayName("payHealthInsuranceForMonthInYear")
    class PayMonth {

        @Test
        @DisplayName("throws when entry not found")
        void notFound() {
            when(repo.findByPatient_IdAndMonthAndYear(5L, Month.JANUARY, 2026))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.payHealthInsuranceForMonthInYear(5L, Month.JANUARY, 2026))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("marks as paid and saves when not already paid")
        void marksAndSaves() {
            HealthInsurance hi = new HealthInsurance();
            hi.setId(1L); hi.setPaid(false);
            when(repo.findByPatient_IdAndMonthAndYear(5L, Month.JANUARY, 2026))
                    .thenReturn(Optional.of(hi));

            service.payHealthInsuranceForMonthInYear(5L, Month.JANUARY, 2026);

            assertThat(hi.isPaid()).isTrue();
            verify(repo).save(hi);
        }

        @Test
        @DisplayName("does nothing (no save) when already paid")
        void alreadyPaid_noop() {
            HealthInsurance hi = new HealthInsurance();
            hi.setId(1L); hi.setPaid(true);
            when(repo.findByPatient_IdAndMonthAndYear(5L, Month.JANUARY, 2026))
                    .thenReturn(Optional.of(hi));

            service.payHealthInsuranceForMonthInYear(5L, Month.JANUARY, 2026);

            verify(repo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("payHealthInsuranceForMonthsInYear")
    class PayMonths {

        @Test
        @DisplayName("throws when any month entry is missing")
        void missingMonth() {
            when(repo.findByPatient_IdAndMonthAndYear(6L, Month.FEBRUARY, 2025))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.payHealthInsuranceForMonthsInYear(6L, Set.of(Month.FEBRUARY), 2025))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FEBRUARY 2025");
        }

        @Test
        @DisplayName("marks only unpaid entries as paid and saves each one")
        void marksOnlyUnpaid() {
            HealthInsurance hiFeb = new HealthInsurance(); hiFeb.setId(1L); hiFeb.setPaid(false);
            HealthInsurance hiMar = new HealthInsurance(); hiMar.setId(2L); hiMar.setPaid(true);

            when(repo.findByPatient_IdAndMonthAndYear(6L, Month.FEBRUARY, 2025)).thenReturn(Optional.of(hiFeb));
            when(repo.findByPatient_IdAndMonthAndYear(6L, Month.MARCH, 2025)).thenReturn(Optional.of(hiMar));

            service.payHealthInsuranceForMonthsInYear(6L, Set.of(Month.FEBRUARY, Month.MARCH), 2025);

            assertThat(hiFeb.isPaid()).isTrue();
            assertThat(hiMar.isPaid()).isTrue();
            verify(repo, times(1)).save(any(HealthInsurance.class));
        }
    }

    @Test
    @DisplayName("referenceForLastSixMonthsByPatientId: returns mapped DTOs, keeps repository order, limits to 6")
    void reference_lastSix() {
        Patient p = new Patient(); p.setId(7L);

        HealthInsurance hi1 = new HealthInsurance(); hi1.setId(1L); hi1.setPatient(p); hi1.setMonth(Month.JUNE); hi1.setYear(2025); hi1.setPaid(true);
        HealthInsurance hi2 = new HealthInsurance(); hi2.setId(2L); hi2.setPatient(p); hi2.setMonth(Month.MAY); hi2.setYear(2025); hi2.setPaid(false);
        HealthInsurance hi3 = new HealthInsurance(); hi3.setId(3L); hi3.setPatient(p); hi3.setMonth(Month.APRIL); hi3.setYear(2025); hi3.setPaid(true);
        HealthInsurance hi4 = new HealthInsurance(); hi4.setId(4L); hi4.setPatient(p); hi4.setMonth(Month.MARCH); hi4.setYear(2025); hi4.setPaid(true);
        HealthInsurance hi5 = new HealthInsurance(); hi5.setId(5L); hi5.setPatient(p); hi5.setMonth(Month.FEBRUARY); hi5.setYear(2025); hi5.setPaid(false);
        HealthInsurance hi6 = new HealthInsurance(); hi6.setId(6L); hi6.setPatient(p); hi6.setMonth(Month.JANUARY); hi6.setYear(2025); hi6.setPaid(true);
        HealthInsurance hi7 = new HealthInsurance(); hi7.setId(7L); hi7.setPatient(p); hi7.setMonth(Month.DECEMBER); hi7.setYear(2024); hi7.setPaid(true);

        when(repo.findAllByPatientOrderByYearMonthDesc(7L))
                .thenReturn(List.of(hi1, hi2, hi3, hi4, hi5, hi6, hi7));

        Set<HealthInsuranceDtoResponse> out = service.referenceForLastSixMonthsByPatientId(7L);

        assertThat(out).hasSize(6);
        assertThat(out.stream().map(HealthInsuranceDtoResponse::getId).toList())
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
        assertThat(out.iterator().next().getMonth()).isEqualTo(Month.JUNE);
    }
}
