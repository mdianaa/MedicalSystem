package org.nbu.medicalrecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.request.DoctorsScheduleDtoRequest;
import org.nbu.medicalrecord.dtos.response.DoctorsScheduleDtoResponse;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.DoctorsSchedule;
import org.nbu.medicalrecord.enums.ShiftType;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.DoctorsScheduleRepository;
import org.nbu.medicalrecord.services.impl.DoctorsScheduleServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorsScheduleServiceImplTest {

    @Mock private DoctorsScheduleRepository scheduleRepo;
    @Mock private DoctorRepository doctorRepo;

    @InjectMocks private DoctorsScheduleServiceImpl service;

    @Nested
    @DisplayName("createNewSchedule")
    class CreateNewScheduleTests {

        @Test
        @DisplayName("throws when doctor is not found")
        void doctorNotFound() {
            DoctorsScheduleDtoRequest req = new DoctorsScheduleDtoRequest();
            req.setDoctorId(42L);
            req.setShift(ShiftType.FIRST_SHIFT);

            when(doctorRepo.findById(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createNewSchedule(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Doctor not found");

            verifyNoInteractions(scheduleRepo);
        }

        @Test
        @DisplayName("creates a NEW schedule when doctor has none; returns mapped DTO")
        void createsNewSchedule() {
            Doctor doctor = new Doctor(); doctor.setId(5L);
            when(doctorRepo.findById(5L)).thenReturn(Optional.of(doctor));

            when(scheduleRepo.findByDoctor_Id(5L)).thenReturn(Optional.empty());

            when(scheduleRepo.save(any(DoctorsSchedule.class))).thenAnswer(inv -> {
                DoctorsSchedule s = inv.getArgument(0);
                s.setId(100L);
                return s;
            });

            DoctorsScheduleDtoRequest req = new DoctorsScheduleDtoRequest();
            req.setDoctorId(5L);
            req.setShift(ShiftType.SECOND_SHIFT);

            DoctorsScheduleDtoResponse out = service.createNewSchedule(req);

            ArgumentCaptor<DoctorsSchedule> captor = ArgumentCaptor.forClass(DoctorsSchedule.class);
            verify(scheduleRepo).save(captor.capture());
            DoctorsSchedule toSave = captor.getValue();

            assertThat(toSave.getDoctor()).isSameAs(doctor);
            assertThat(toSave.getShift()).isEqualTo(ShiftType.SECOND_SHIFT);

            assertThat(out.getScheduleId()).isEqualTo(100L);
            assertThat(out.getDoctorId()).isEqualTo(5L);
            assertThat(out.getShift()).isEqualTo(ShiftType.SECOND_SHIFT);
        }

        @Test
        @DisplayName("REUSES existing schedule for the same doctor and updates shift")
        void reusesExistingSchedule() {
            Doctor doctor = new Doctor(); doctor.setId(7L);
            when(doctorRepo.findById(7L)).thenReturn(Optional.of(doctor));

            DoctorsSchedule existing = new DoctorsSchedule();
            existing.setId(55L);
            existing.setDoctor(doctor);
            existing.setShift(ShiftType.FIRST_SHIFT);

            when(scheduleRepo.findByDoctor_Id(7L)).thenReturn(Optional.of(existing));
            when(scheduleRepo.save(existing)).thenReturn(existing);

            DoctorsScheduleDtoRequest req = new DoctorsScheduleDtoRequest();
            req.setDoctorId(7L);
            req.setShift(ShiftType.NIGHT_SHIFT);

            DoctorsScheduleDtoResponse out = service.createNewSchedule(req);

            verify(scheduleRepo).save(existing);
            assertThat(existing.getShift()).isEqualTo(ShiftType.NIGHT_SHIFT);

            assertThat(out.getScheduleId()).isEqualTo(55L);
            assertThat(out.getDoctorId()).isEqualTo(7L);
            assertThat(out.getShift()).isEqualTo(ShiftType.NIGHT_SHIFT);
        }
    }

    @Nested
    @DisplayName("deleteSchedule")
    class DeleteScheduleTests {

        @Test
        @DisplayName("throws when schedule not found")
        void scheduleNotFound() {
            when(scheduleRepo.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.deleteSchedule(1L, 99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Schedule not found");
            verify(scheduleRepo, never()).delete(any());
        }

        @Test
        @DisplayName("throws when schedule does NOT belong to the given doctor")
        void wrongOwner() {
            Doctor other = new Doctor(); other.setId(2L);
            DoctorsSchedule schedule = new DoctorsSchedule(); schedule.setId(9L); schedule.setDoctor(other);

            when(scheduleRepo.findById(9L)).thenReturn(Optional.of(schedule));

            assertThatThrownBy(() -> service.deleteSchedule(1L, 9L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not belong");
            verify(scheduleRepo, never()).delete(any());
        }

        @Test
        @DisplayName("deletes when schedule belongs to the doctor")
        void deletesOk() {
            Doctor d = new Doctor(); d.setId(4L);
            DoctorsSchedule schedule = new DoctorsSchedule(); schedule.setId(12L); schedule.setDoctor(d);

            when(scheduleRepo.findById(12L)).thenReturn(Optional.of(schedule));

            service.deleteSchedule(4L, 12L);

            verify(scheduleRepo).delete(schedule);
        }
    }
}
