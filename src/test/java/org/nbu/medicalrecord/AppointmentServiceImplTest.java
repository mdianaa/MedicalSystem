package org.nbu.medicalrecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.request.AppointmentDtoRequest;
import org.nbu.medicalrecord.dtos.response.DoctorAppointmentDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientAppointmentDtoResponse;
import org.nbu.medicalrecord.entities.Appointment;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.entities.User;
import org.nbu.medicalrecord.repositories.AppointmentRepository;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.impl.AppointmentServiceImpl;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AppointmentServiceImpl service;

    private static Doctor mkDoctor(long id, String first, String last) {
        var d = new Doctor();
        d.setId(id);
        var u = new User();
        u.setFirstName(first);
        u.setLastName(last);
        d.setUser(u);
        return d;
    }

    private static Patient mkPatient(long id, String first, String last) {
        var p = new Patient();
        p.setId(id);
        var u = new User();
        u.setFirstName(first);
        u.setLastName(last);
        p.setUser(u);
        return p;
    }

    private static Appointment mkAppt(Long id, Doctor d, Patient p, LocalDate date, LocalTime time) {
        var a = new Appointment();
        a.setId(id);
        a.setDoctor(d);
        a.setPatient(p);
        a.setDate(date);
        a.setHourOfAppointment(time);
        return a;
    }

    @Nested
    class MakeAppointmentTests {

        @Test
        @DisplayName("rejects past dates")
        void rejectsPast() {
            var req = new AppointmentDtoRequest(LocalDate.now().minusDays(1), LocalTime.of(10, 0), 1L, 2L);
            assertThatThrownBy(() -> service.makeAppointment(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("past");
            verifyNoInteractions(appointmentRepository, doctorRepository, patientRepository);
        }

        @Test
        @DisplayName("throws when doctor not found")
        void doctorNotFound() {
            var req = new AppointmentDtoRequest(LocalDate.now().plusDays(1), LocalTime.of(9, 0), 99L, 1L);
            when(doctorRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.makeAppointment(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Doctor not found");
        }

        @Test
        @DisplayName("throws when patient not found")
        void patientNotFound() {
            var req = new AppointmentDtoRequest(LocalDate.now().plusDays(1), LocalTime.of(9, 0), 5L, 77L);
            when(doctorRepository.findById(5L)).thenReturn(Optional.of(mkDoctor(5L, "Ana", "Dimitrova")));
            when(patientRepository.findById(77L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.makeAppointment(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Patient not found");
        }

        @Test
        @DisplayName("throws when slot already booked (existsBy... = true)")
        void slotAlreadyBooked_fastCheck() {
            var date = LocalDate.now().plusDays(2);
            var time = LocalTime.of(10, 30);
            var req = new AppointmentDtoRequest(date, time, 5L, 7L);

            when(doctorRepository.findById(5L)).thenReturn(Optional.of(mkDoctor(5L, "Ana", "Dimitrova")));
            when(patientRepository.findById(7L)).thenReturn(Optional.of(mkPatient(7L, "Ivan", "Petrov")));
            when(appointmentRepository.existsByDoctor_IdAndDateAndHourOfAppointment(5L, date, time))
                    .thenReturn(true);

            assertThatThrownBy(() -> service.makeAppointment(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already");
            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("saves successfully and returns PatientAppointmentDtoResponse with doctorName")
        void happyPath() {
            var date = LocalDate.now().plusDays(3);
            var time = LocalTime.of(11, 0);
            var req = new AppointmentDtoRequest(date, time, 5L, 7L);

            var d = mkDoctor(5L, "Ana", "Dimitrova");
            var p = mkPatient(7L, "Ivan", "Petrov");

            when(doctorRepository.findById(5L)).thenReturn(Optional.of(d));
            when(patientRepository.findById(7L)).thenReturn(Optional.of(p));
            when(appointmentRepository.existsByDoctor_IdAndDateAndHourOfAppointment(5L, date, time)).thenReturn(false);

            // assign id on save
            doAnswer(inv -> {
                Appointment a = inv.getArgument(0);
                a.setId(123L);
                return null;
            }).when(appointmentRepository).save(any(Appointment.class));

            PatientAppointmentDtoResponse dto = service.makeAppointment(req);

            ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
            verify(appointmentRepository).save(captor.capture());
            Appointment saved = captor.getValue();

            assertThat(saved.getDoctor().getId()).isEqualTo(5L);
            assertThat(saved.getPatient().getId()).isEqualTo(7L);
            assertThat(saved.getDate()).isEqualTo(date);
            assertThat(saved.getHourOfAppointment()).isEqualTo(time);

            assertThat(dto.getId()).isEqualTo(123L);
            assertThat(dto.getDoctorId()).isEqualTo(5L);
            assertThat(dto.getPatientId()).isEqualTo(7L);
            assertThat(dto.getDoctorName()).isEqualTo("Ana Dimitrova");
            assertThat(dto.getDate()).isEqualTo(date);
            assertThat(dto.getHourOfAppointment()).isEqualTo(time);
        }

        @Test
        @DisplayName("wraps DataIntegrityViolationException into IllegalStateException (race condition)")
        void raceCondition() {
            var date = LocalDate.now().plusDays(1);
            var time = LocalTime.of(9, 0);
            var req = new AppointmentDtoRequest(date, time, 5L, 7L);

            when(doctorRepository.findById(5L)).thenReturn(Optional.of(mkDoctor(5L, "A", "B")));
            when(patientRepository.findById(7L)).thenReturn(Optional.of(mkPatient(7L, "C", "D")));
            when(appointmentRepository.existsByDoctor_IdAndDateAndHourOfAppointment(5L, date, time)).thenReturn(false);
            doThrow(new DataIntegrityViolationException("unique constraint"))
                    .when(appointmentRepository).save(any(Appointment.class));

            assertThatThrownBy(() -> service.makeAppointment(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("just booked");
        }
    }

    @Test
    @DisplayName("showAllAvailableAppointmentsByDoctorId: filters by patient null, sorts by date then time, maps DTO")
    void showAvailable_sortedAndMapped() {
        var d = mkDoctor(2L, "Ana", "Dimitrova");
        var date = LocalDate.now().plusDays(1);
        var a1 = mkAppt(1L, d, null, date.plusDays(1), LocalTime.of(14, 0));
        var a2 = mkAppt(2L, d, null, date, LocalTime.of(9, 0));
        var a3 = mkAppt(3L, d, null, date, LocalTime.of(8, 0));

        when(appointmentRepository.findByDoctor_IdAndPatientIsNullAndDateGreaterThanEqual(eq(2L), any(LocalDate.class)))
                .thenReturn(List.of(a1, a2, a3));

        Set<PatientAppointmentDtoResponse> out = service.showAllAvailableAppointmentsByDoctorId(2L);
        assertThat(out).extracting(PatientAppointmentDtoResponse::getId).containsExactly(3L, 2L, 1L); // sorted
        assertThat(out).allMatch(dto -> dto.getDoctorName().equals("Ana Dimitrova"));
    }

    @Test
    @DisplayName("showAllPatientAppointmentsById: sorts by date then time, maps DTO")
    void showPatientAppointments_sorted() {
        var d = mkDoctor(5L, "Ana", "Dimitrova");
        var p = mkPatient(9L, "Ivan", "Petrov");
        var today = LocalDate.now();

        var a1 = mkAppt(10L, d, p, today.plusDays(2), LocalTime.of(12, 0));
        var a2 = mkAppt(11L, d, p, today.plusDays(1), LocalTime.of(16, 0));
        var a3 = mkAppt(12L, d, p, today.plusDays(1), LocalTime.of(9, 0));

        when(appointmentRepository.findByPatient_Id(9L)).thenReturn(List.of(a1, a2, a3));

        Set<PatientAppointmentDtoResponse> out = service.showAllPatientAppointmentsById(9L);
        assertThat(out).extracting(PatientAppointmentDtoResponse::getId).containsExactly(12L, 11L, 10L);
        assertThat(out).allMatch(dto -> dto.getDoctorId().equals(5L));
    }

    @Test
    @DisplayName("showPatientAppointmentOnDateById: returns DTO when exists, throws when missing")
    void showPatientAppointmentOnDateById_behaviour() {
        var d = mkDoctor(1L, "Ana", "D");
        var p = mkPatient(2L, "Ivan", "P");
        var date = LocalDate.now().plusDays(5);
        var appt = mkAppt(33L, d, p, date, LocalTime.of(10, 0));

        when(appointmentRepository.findByPatient_IdAndDate(2L, date)).thenReturn(Optional.of(appt));

        var dto = service.showPatientAppointmentOnDateById(2L, date);
        assertThat(dto.getId()).isEqualTo(33L);
        assertThat(dto.getDoctorName()).isEqualTo("Ana D");

        when(appointmentRepository.findByPatient_IdAndDate(2L, date.plusDays(1))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.showPatientAppointmentOnDateById(2L, date.plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No appointment found");
    }

    @Test
    @DisplayName("showAllOccupiedAppointmentsById: only with patient != null, sorted and mapped to DoctorAppointmentDtoResponse")
    void showOccupied_sortedAndMapped() {
        var d = mkDoctor(4L, "Ana", "D");
        var p1 = mkPatient(7L, "Ivan", "P");
        var p2 = mkPatient(8L, "Maya", "I");
        var today = LocalDate.now();

        var a1 = mkAppt(1L, d, p2, today.plusDays(1), LocalTime.of(11, 0));
        var a2 = mkAppt(2L, d, p1, today, LocalTime.of(9, 0));
        var a3 = mkAppt(3L, d, p1, today, LocalTime.of(8, 0));

        when(appointmentRepository.findByDoctor_IdAndPatientIsNotNull(4L)).thenReturn(List.of(a1, a2, a3));

        Set<DoctorAppointmentDtoResponse> out = service.showAllOccupiedAppointmentsById(4L);
        assertThat(out).extracting(DoctorAppointmentDtoResponse::getId).containsExactly(3L, 2L, 1L);
        assertThat(out).anySatisfy(dto -> assertThat(dto.getPatientName()).isEqualTo("Maya I"));
        assertThat(out).anySatisfy(dto -> assertThat(dto.getPatientName()).isEqualTo("Ivan P"));
    }

    @Test
    @DisplayName("cancelAppointment: delegates to repository.deleteById")
    void cancelAppointment_deletes() {
        service.cancelAppointment(55L);
        verify(appointmentRepository).deleteById(55L);
    }
}

