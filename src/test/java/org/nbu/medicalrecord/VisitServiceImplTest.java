package org.nbu.medicalrecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.request.VisitDtoRequest;
import org.nbu.medicalrecord.dtos.response.VisitDtoResponse;
import org.nbu.medicalrecord.entities.*;
import org.nbu.medicalrecord.repositories.*;
import org.nbu.medicalrecord.services.impl.VisitServiceImpl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitServiceImplTest {

    @Mock
    private VisitRepository visitRepo;

    @Mock
    private AppointmentRepository appointmentRepo;

    @Mock
    private DiagnosisRepository diagnosisRepo;

    @Mock
    private SickLeaveRepository sickLeaveRepo;

    @Mock
    private MedicalRecordRepository medicalRecordRepo;

    @InjectMocks
    private VisitServiceImpl service;

    private User user(String first, String last) {
        User u = new User();
        u.setFirstName(first);
        u.setLastName(last);
        return u;
    }

    private Doctor doctor(long id, String f, String l) {
        Doctor d = new Doctor();
        d.setId(id);
        d.setUser(user(f, l));
        return d;
    }

    private Patient patient(long id, String f, String l) {
        Patient p = new Patient();
        p.setId(id);
        p.setUser(user(f, l));
        return p;
    }

    private Appointment appt(long id, Doctor d, Patient p, LocalDate date, LocalTime time) {
        Appointment a = new Appointment();
        a.setId(id);
        a.setDoctor(d);
        a.setPatient(p);
        a.setDate(date);
        a.setHourOfAppointment(time);
        return a;
    }

    private MedicalRecord record(long id, Patient p) {
        MedicalRecord r = new MedicalRecord();
        r.setId(id);
        r.setPatient(p);
        return r;
    }

    private Diagnosis diagnosis(long id, Doctor d, Patient p) {
        Diagnosis diag = new Diagnosis();
        diag.setId(id);
        diag.setDoctor(d);
        diag.setPatient(p);
        return diag;
    }

    private SickLeave sickLeave(long id, Doctor d, Patient p) {
        SickLeave s = new SickLeave();
        s.setId(id);
        s.setDoctor(d);
        s.setPatient(p);
        return s;
    }

    // ---------------------------
    // createNewVisit
    // ---------------------------
    @Nested
    @DisplayName("createNewVisit")
    class CreateNewVisitTests {

        @Test
        @DisplayName("throws if appointment not found")
        void appointmentNotFound_throws() {
            VisitDtoRequest req = new VisitDtoRequest();
            req.setAppointmentId(99L);

            when(appointmentRepo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createNewVisit(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Appointment not found");

            verifyNoInteractions(visitRepo, medicalRecordRepo, diagnosisRepo, sickLeaveRepo);
        }

        @Test
        @DisplayName("throws if a visit already exists for that appointment")
        void visitAlreadyExists_throws() {
            Doctor d = doctor(10L, "Ana", "Dimitrova");
            Patient p = patient(20L, "Ivan", "Petrov");
            Appointment a = appt(5L, d, p, LocalDate.of(2025, 10, 30), LocalTime.of(9, 30));

            VisitDtoRequest req = new VisitDtoRequest();
            req.setAppointmentId(5L);

            when(appointmentRepo.findById(5L)).thenReturn(Optional.of(a));
            when(visitRepo.existsByAppointment_Id(5L)).thenReturn(true);

            assertThatThrownBy(() -> service.createNewVisit(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already exists");

            verify(visitRepo, never()).save(any());
        }

        @Test
        @DisplayName("throws if appointment has no patient")
        void noPatientOnAppointment_throws() {
            Doctor d = doctor(10L, "Ana", "Dimitrova");
            Appointment a = appt(5L, d, null, LocalDate.of(2025, 10, 30), LocalTime.of(9, 30));

            VisitDtoRequest req = new VisitDtoRequest();
            req.setAppointmentId(5L);

            when(appointmentRepo.findById(5L)).thenReturn(Optional.of(a));
            when(visitRepo.existsByAppointment_Id(5L)).thenReturn(false);

            assertThatThrownBy(() -> service.createNewVisit(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not booked by a patient");

            verify(visitRepo, never()).save(any());
        }

        @Test
        @DisplayName("throws if medical record for patient is missing")
        void noMedicalRecord_throws() {
            Doctor d = doctor(10L, "Ana", "Dimitrova");
            Patient p = patient(20L, "Ivan", "Petrov");
            Appointment a = appt(5L, d, p, LocalDate.of(2025, 10, 30), LocalTime.of(9, 30));

            VisitDtoRequest req = new VisitDtoRequest();
            req.setAppointmentId(5L);

            when(appointmentRepo.findById(5L)).thenReturn(Optional.of(a));
            when(visitRepo.existsByAppointment_Id(5L)).thenReturn(false);
            when(medicalRecordRepo.findByPatient_Id(20L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createNewVisit(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Medical record not found");

            verify(visitRepo, never()).save(any());
        }

        @Test
        @DisplayName("throws if diagnosisId provided but diagnosis not found")
        void diagnosisMissing_throws() {
            Doctor d = doctor(10L, "Ana", "Dimitrova");
            Patient p = patient(20L, "Ivan", "Petrov");
            Appointment a = appt(5L, d, p, LocalDate.of(2025, 10, 30), LocalTime.of(9, 30));
            MedicalRecord rec = record(100L, p);

            VisitDtoRequest req = new VisitDtoRequest();
            req.setAppointmentId(5L);
            req.setDiagnosisId(555L); // ask for a diagnosis that won't be found

            when(appointmentRepo.findById(5L)).thenReturn(Optional.of(a));
            when(visitRepo.existsByAppointment_Id(5L)).thenReturn(false);
            when(medicalRecordRepo.findByPatient_Id(20L)).thenReturn(Optional.of(rec));
            when(diagnosisRepo.findById(555L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createNewVisit(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Diagnosis not found");

            verify(visitRepo, never()).save(any());
        }

        @Test
        @DisplayName("throws if diagnosis doesn't belong to same doctor/patient")
        void diagnosisMismatch_throws() {
            Doctor doctorA = doctor(10L, "Ana", "Dimitrova");
            Patient patientA = patient(20L, "Ivan", "Petrov");
            Appointment a = appt(5L, doctorA, patientA, LocalDate.of(2025, 10, 30), LocalTime.of(9, 30));
            MedicalRecord rec = record(100L, patientA);

            // Diagnosis is for some other combo
            Doctor doctorB = doctor(99L, "Other", "Doctor");
            Patient patientB = patient(88L, "Other", "Patient");
            Diagnosis wrongDiag = diagnosis(555L, doctorB, patientB);

            VisitDtoRequest req = new VisitDtoRequest();
            req.setAppointmentId(5L);
            req.setDiagnosisId(555L);

            when(appointmentRepo.findById(5L)).thenReturn(Optional.of(a));
            when(visitRepo.existsByAppointment_Id(5L)).thenReturn(false);
            when(medicalRecordRepo.findByPatient_Id(20L)).thenReturn(Optional.of(rec));
            when(diagnosisRepo.findById(555L)).thenReturn(Optional.of(wrongDiag));

            assertThatThrownBy(() -> service.createNewVisit(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Diagnosis must belong to the same doctor and patient");

            verify(visitRepo, never()).save(any());
        }

        @Test
        @DisplayName("throws if sickLeaveId provided but sick leave not found")
        void sickLeaveMissing_throws() {
            Doctor d = doctor(10L, "Ana", "Dimitrova");
            Patient p = patient(20L, "Ivan", "Petrov");
            Appointment a = appt(5L, d, p, LocalDate.of(2025, 10, 30), LocalTime.of(9, 30));
            MedicalRecord rec = record(100L, p);

            VisitDtoRequest req = new VisitDtoRequest();
            req.setAppointmentId(5L);
            req.setSickLeaveId(999L);

            when(appointmentRepo.findById(5L)).thenReturn(Optional.of(a));
            when(visitRepo.existsByAppointment_Id(5L)).thenReturn(false);
            when(medicalRecordRepo.findByPatient_Id(20L)).thenReturn(Optional.of(rec));
            when(sickLeaveRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createNewVisit(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Sick leave not found");

            verify(visitRepo, never()).save(any());
        }

        @Test
        @DisplayName("throws if sick leave doesn't belong to same doctor/patient")
        void sickLeaveMismatch_throws() {
            Doctor doctorA = doctor(10L, "Ana", "Dimitrova");
            Patient patientA = patient(20L, "Ivan", "Petrov");
            Appointment a = appt(5L, doctorA, patientA, LocalDate.of(2025, 10, 30), LocalTime.of(9, 30));
            MedicalRecord rec = record(100L, patientA);

            Doctor doctorB = doctor(77L, "Other", "Doc");
            Patient patientB = patient(88L, "Other", "Pat");
            SickLeave wrongSL = sickLeave(999L, doctorB, patientB);

            VisitDtoRequest req = new VisitDtoRequest();
            req.setAppointmentId(5L);
            req.setSickLeaveId(999L);

            when(appointmentRepo.findById(5L)).thenReturn(Optional.of(a));
            when(visitRepo.existsByAppointment_Id(5L)).thenReturn(false);
            when(medicalRecordRepo.findByPatient_Id(20L)).thenReturn(Optional.of(rec));
            when(sickLeaveRepo.findById(999L)).thenReturn(Optional.of(wrongSL));

            assertThatThrownBy(() -> service.createNewVisit(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Sick leave must belong to the same doctor and patient");

            verify(visitRepo, never()).save(any());
        }

        @Test
        @DisplayName("saves Visit and returns VisitDtoResponse with proper mapping")
        void saveVisitSuccessfully() {
            Doctor d = doctor(10L, "Ana", "Dimitrova");
            Patient p = patient(20L, "Ivan", "Petrov");
            Appointment a = appt(5L, d, p, LocalDate.of(2025, 10, 30), LocalTime.of(9, 30));
            MedicalRecord rec = record(100L, p);

            Diagnosis diag = diagnosis(500L, d, p);
            SickLeave sl = sickLeave(600L, d, p);

            VisitDtoRequest req = new VisitDtoRequest();
            req.setAppointmentId(5L);
            req.setDiagnosisId(500L);
            req.setSickLeaveId(600L);

            when(appointmentRepo.findById(5L)).thenReturn(Optional.of(a));
            when(visitRepo.existsByAppointment_Id(5L)).thenReturn(false);
            when(medicalRecordRepo.findByPatient_Id(20L)).thenReturn(Optional.of(rec));
            when(diagnosisRepo.findById(500L)).thenReturn(Optional.of(diag));
            when(sickLeaveRepo.findById(600L)).thenReturn(Optional.of(sl));

            when(visitRepo.save(any(Visit.class))).thenAnswer(inv -> {
                Visit v = inv.getArgument(0);
                v.setId(123L);
                return v;
            });

            VisitDtoResponse out = service.createNewVisit(req);

            // capture what we saved
            ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);
            verify(visitRepo).save(captor.capture());
            Visit saved = captor.getValue();

            assertThat(saved.getAppointment()).isSameAs(a);
            assertThat(saved.getDiagnosis()).isSameAs(diag);
            assertThat(saved.getSickLeave()).isSameAs(sl);
            assertThat(saved.getMedicalRecord()).isSameAs(rec);

            // assert response fields
            assertThat(out.getId()).isEqualTo(123L);
            assertThat(out.getAppointmentId()).isEqualTo(5L);
            assertThat(out.getDoctorId()).isEqualTo(10L);
            assertThat(out.getDoctorName()).isEqualTo("Ana Dimitrova");
            assertThat(out.getPatientId()).isEqualTo(20L);
            assertThat(out.getPatientName()).isEqualTo("Ivan Petrov");
            assertThat(out.getMedicalRecordId()).isEqualTo(100L);
            assertThat(out.getDate()).isEqualTo(LocalDate.of(2025, 10, 30));
            assertThat(out.getDiagnosisId()).isEqualTo(500L);
            assertThat(out.getSickLeaveId()).isEqualTo(600L);
        }
    }

    // ---------------------------
    // showAllVisitsByDoctor
    // ---------------------------
    @Test
    @DisplayName("showAllVisitsByDoctor maps visits from repo into VisitDtoResponse, preserving repo order via LinkedHashSet")
    void showAllVisitsByDoctor_maps() {
        Doctor d = doctor(10L, "Ana", "Dimitrova");
        Patient p = patient(20L, "Ivan", "Petrov");
        MedicalRecord rec = record(100L, p);
        Appointment a1 = appt(1L, d, p, LocalDate.of(2025, 10, 30), LocalTime.of(9, 0));
        Appointment a2 = appt(2L, d, p, LocalDate.of(2025, 10, 29), LocalTime.of(10, 0));

        Visit v1 = new Visit();
        v1.setId(101L);
        v1.setAppointment(a1);
        v1.setMedicalRecord(rec);

        Visit v2 = new Visit();
        v2.setId(102L);
        v2.setAppointment(a2);
        v2.setMedicalRecord(rec);

        when(visitRepo.findByAppointment_Doctor_IdOrderByAppointment_DateDesc(10L))
                .thenReturn(List.of(v1, v2));

        Set<VisitDtoResponse> out = service.showAllVisitsByDoctor(10L);

        verify(visitRepo).findByAppointment_Doctor_IdOrderByAppointment_DateDesc(10L);

        assertThat(out).hasSize(2);

        Iterator<VisitDtoResponse> it = out.iterator();
        VisitDtoResponse first = it.next();
        VisitDtoResponse second = it.next();

        assertThat(first.getId()).isEqualTo(101L);
        assertThat(first.getDoctorName()).isEqualTo("Ana Dimitrova");
        assertThat(first.getPatientName()).isEqualTo("Ivan Petrov");
        assertThat(first.getDate()).isEqualTo(LocalDate.of(2025, 10, 30));

        assertThat(second.getId()).isEqualTo(102L);
        assertThat(second.getDate()).isEqualTo(LocalDate.of(2025, 10, 29));
    }

    // ---------------------------
    // showAllVisitsForPatient
    // ---------------------------
    @Test
    @DisplayName("showAllVisitsForPatient maps visits from repo into VisitDtoResponse")
    void showAllVisitsForPatient_maps() {
        Doctor d = doctor(10L, "Ana", "Dimitrova");
        Patient p = patient(20L, "Ivan", "Petrov");
        MedicalRecord rec = record(100L, p);
        Appointment a = appt(1L, d, p, LocalDate.of(2025, 8, 15), LocalTime.of(11, 0));

        Visit v = new Visit();
        v.setId(200L);
        v.setAppointment(a);
        v.setMedicalRecord(rec);

        when(visitRepo.findByMedicalRecord_Patient_IdOrderByAppointment_DateDesc(20L))
                .thenReturn(List.of(v));

        Set<VisitDtoResponse> out = service.showAllVisitsForPatient(20L);

        verify(visitRepo).findByMedicalRecord_Patient_IdOrderByAppointment_DateDesc(20L);

        assertThat(out).hasSize(1);
        VisitDtoResponse dto = out.iterator().next();
        assertThat(dto.getId()).isEqualTo(200L);
        assertThat(dto.getPatientId()).isEqualTo(20L);
        assertThat(dto.getDoctorId()).isEqualTo(10L);
        assertThat(dto.getDoctorName()).isEqualTo("Ana Dimitrova");
        assertThat(dto.getPatientName()).isEqualTo("Ivan Petrov");
        assertThat(dto.getDate()).isEqualTo(LocalDate.of(2025, 8, 15));
    }

    // ---------------------------
    // showAllVisitsForPatientInPeriod
    // ---------------------------
    @Nested
    @DisplayName("showAllVisitsForPatientInPeriod")
    class ShowAllVisitsForPatientInPeriodTests {

        @Test
        @DisplayName("throws if from is null or to is null")
        void nullRange_throws() {
            assertThatThrownBy(() ->
                    service.showAllVisitsForPatientInPeriod(1L, null, LocalDate.now()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("'from' and 'to' must be provided");

            assertThatThrownBy(() ->
                    service.showAllVisitsForPatientInPeriod(1L, LocalDate.now(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("'from' and 'to' must be provided");
        }

        @Test
        @DisplayName("throws if from is after to")
        void invalidRangeOrder_throws() {
            LocalDate from = LocalDate.of(2025, 10, 30);
            LocalDate to   = LocalDate.of(2025, 10, 1);

            assertThatThrownBy(() ->
                    service.showAllVisitsForPatientInPeriod(1L, from, to))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("'from' must be on or before 'to'");
        }

        @Test
        @DisplayName("returns mapped visits within range")
        void returnMappedVisits() {
            Doctor d = doctor(10L, "Ana", "Dimitrova");
            Patient p = patient(20L, "Ivan", "Petrov");
            MedicalRecord rec = record(100L, p);
            Appointment a = appt(1L, d, p, LocalDate.of(2025, 9, 15), LocalTime.of(14, 0));

            Visit v = new Visit();
            v.setId(300L);
            v.setAppointment(a);
            v.setMedicalRecord(rec);

            LocalDate from = LocalDate.of(2025, 9, 1);
            LocalDate to   = LocalDate.of(2025, 9, 30);

            when(visitRepo.findByMedicalRecord_Patient_IdAndAppointment_DateBetweenOrderByAppointment_DateDesc(
                    20L, from, to
            )).thenReturn(List.of(v));

            Set<VisitDtoResponse> out = service.showAllVisitsForPatientInPeriod(20L, from, to);

            verify(visitRepo).findByMedicalRecord_Patient_IdAndAppointment_DateBetweenOrderByAppointment_DateDesc(
                    20L, from, to);

            assertThat(out).hasSize(1);
            VisitDtoResponse dto = out.iterator().next();
            assertThat(dto.getId()).isEqualTo(300L);
            assertThat(dto.getDoctorName()).isEqualTo("Ana Dimitrova");
            assertThat(dto.getPatientName()).isEqualTo("Ivan Petrov");
            assertThat(dto.getDate()).isEqualTo(LocalDate.of(2025, 9, 15));
        }
    }

    // ---------------------------
    // showAllVisitsByDoctorInPeriod
    // ---------------------------
    @Nested
    @DisplayName("showAllVisitsByDoctorInPeriod")
    class ShowAllVisitsByDoctorInPeriodTests {

        @Test
        @DisplayName("throws if from or to is null")
        void nullRange_throws() {
            assertThatThrownBy(() ->
                    service.showAllVisitsByDoctorInPeriod(1L, null, LocalDate.now()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("'from' and 'to' must be provided");

            assertThatThrownBy(() ->
                    service.showAllVisitsByDoctorInPeriod(1L, LocalDate.now(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("'from' and 'to' must be provided");
        }

        @Test
        @DisplayName("throws if from is after to")
        void invalidRangeOrder_throws() {
            LocalDate from = LocalDate.of(2025, 10, 30);
            LocalDate to   = LocalDate.of(2025, 10, 1);

            assertThatThrownBy(() ->
                    service.showAllVisitsByDoctorInPeriod(1L, from, to))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("'from' must be on or before 'to'");
        }

        @Test
        @DisplayName("returns mapped visits for doctor within range")
        void returnMapped() {
            Doctor d = doctor(10L, "Ana", "Dimitrova");
            Patient p = patient(20L, "Ivan", "Petrov");
            MedicalRecord rec = record(100L, p);
            Appointment a = appt(10L, d, p, LocalDate.of(2025, 7, 7), LocalTime.of(8, 30));

            Visit v = new Visit();
            v.setId(400L);
            v.setAppointment(a);
            v.setMedicalRecord(rec);

            LocalDate from = LocalDate.of(2025, 7, 1);
            LocalDate to   = LocalDate.of(2025, 7, 31);

            when(visitRepo.findByAppointment_Doctor_IdAndAppointment_DateBetweenOrderByAppointment_DateDesc(
                    10L, from, to
            )).thenReturn(List.of(v));

            Set<VisitDtoResponse> out = service.showAllVisitsByDoctorInPeriod(10L, from, to);

            verify(visitRepo).findByAppointment_Doctor_IdAndAppointment_DateBetweenOrderByAppointment_DateDesc(
                    10L, from, to);

            assertThat(out).hasSize(1);
            VisitDtoResponse dto = out.iterator().next();
            assertThat(dto.getId()).isEqualTo(400L);
            assertThat(dto.getDoctorId()).isEqualTo(10L);
            assertThat(dto.getDoctorName()).isEqualTo("Ana Dimitrova");
            assertThat(dto.getPatientName()).isEqualTo("Ivan Petrov");
            assertThat(dto.getDate()).isEqualTo(LocalDate.of(2025, 7, 7));
        }
    }
}
