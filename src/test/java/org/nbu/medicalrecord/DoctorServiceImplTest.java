package org.nbu.medicalrecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.response.DoctorDataDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientDataDtoResponse;
import org.nbu.medicalrecord.dtos.response.SpecializationDtoResponse;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.Patient;
import org.nbu.medicalrecord.entities.Specialization;
import org.nbu.medicalrecord.entities.User;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.services.impl.DoctorServiceImpl;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private DoctorServiceImpl service;

    @Nested
    @DisplayName("addNewPatientForGpById")
    class AddNewPatientForGpById {

        @Test
        @DisplayName("throws when doctor not found")
        void doctorNotFound() {
            when(doctorRepository.findById(9L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.addNewPatientForGpById(1L, 9L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Doctor not found");
            verifyNoInteractions(patientRepository);
        }

        @Test
        @DisplayName("throws when doctor exists but is not GP")
        void doctorNotGp() {
            Doctor d = new Doctor();
            d.setId(5L);
            d.setGp(false);
            when(doctorRepository.findById(5L)).thenReturn(Optional.of(d));

            assertThatThrownBy(() -> service.addNewPatientForGpById(1L, 5L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not a GP");
            verifyNoInteractions(patientRepository);
        }

        @Test
        @DisplayName("throws when patient not found")
        void patientNotFound() {
            Doctor gp = new Doctor();
            gp.setId(2L);
            gp.setGp(true);
            when(doctorRepository.findById(2L)).thenReturn(Optional.of(gp));
            when(patientRepository.findById(101L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addNewPatientForGpById(101L, 2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Patient not found");
            verify(patientRepository, never()).save(any());
        }

        @Test
        @DisplayName("sets patient's GP and returns PatientDataDtoResponse")
        void happyPath() {
            Doctor gp = new Doctor();
            gp.setId(3L);
            gp.setGp(true);
            when(doctorRepository.findById(3L)).thenReturn(Optional.of(gp));

            Patient p = new Patient();
            p.setId(7L);
            User u = new User(); u.setFirstName("Ana"); u.setLastName("Ivanova");
            p.setUser(u);
            p.setBirthDate(LocalDate.of(1995, 5, 15));
            when(patientRepository.findById(7L)).thenReturn(Optional.of(p));

            when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

            PatientDataDtoResponse out = service.addNewPatientForGpById(7L, 3L);

            ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
            verify(patientRepository).save(captor.capture());
            Patient toSave = captor.getValue();
            assertThat(toSave.getGp()).isSameAs(gp);

            assertThat(out.getId()).isEqualTo(7L);
            assertThat(out.getFirstName()).isEqualTo("Ana");
            assertThat(out.getLastName()).isEqualTo("Ivanova");
            assertThat(out.getBirthDate()).isEqualTo(LocalDate.of(1995, 5, 15));
        }
    }

    @Nested
    @DisplayName("countTotalPatientsForGpById")
    class CountPatientsForGp {

        @Test
        @DisplayName("throws when doctor (gp) not found")
        void gpMissing() {
            when(doctorRepository.findById(88L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.countTotalPatientsForGpById(88L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Doctor not found");
        }

        @Test
        @DisplayName("returns 0 when gpPatients is null")
        void nullSetReturnsZero() {
            Doctor gp = new Doctor();
            gp.setId(1L);
            gp.setGp(true);
            gp.setGpPatients(null);
            when(doctorRepository.findById(1L)).thenReturn(Optional.of(gp));

            assertThat(service.countTotalPatientsForGpById(1L)).isEqualTo(0);
        }

        @Test
        @DisplayName("returns the size of gpPatients when present")
        void returnsSize() {
            Doctor gp = new Doctor();
            gp.setId(2L);
            gp.setGp(true);
            Set<Patient> pts = new LinkedHashSet<>();
            pts.add(new Patient()); pts.add(new Patient());
            gp.setGpPatients(pts);
            when(doctorRepository.findById(2L)).thenReturn(Optional.of(gp));

            assertThat(service.countTotalPatientsForGpById(2L)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("showAllGPs: maps doctors (gp=true) to DoctorDataDtoResponse with spec and patient count")
    void showAllGPs_maps() {
        Doctor d1 = new Doctor();
        d1.setId(10L);
        d1.setGp(true);
        User u1 = new User(); u1.setId(100L);
        d1.setUser(u1);
        Specialization s1 = new Specialization(); s1.setId(501L); s1.setType("Cardiology");
        d1.setSpecialization(s1);
        d1.setGpPatients(new LinkedHashSet<>(List.of(new Patient(), new Patient())));

        Doctor d2 = new Doctor();
        d2.setId(11L);
        d2.setGp(true);
        User u2 = new User(); u2.setId(101L);
        d2.setUser(u2);

        when(doctorRepository.findByGpTrue()).thenReturn(List.of(d1, d2));

        Set<DoctorDataDtoResponse> out = service.showAllGPs();
        assertThat(out).hasSize(2);

        DoctorDataDtoResponse r1 = out.stream().filter(r -> r.getId().equals(10L)).findFirst().orElseThrow();
        assertThat(r1.getUserId()).isEqualTo(100L);
        assertThat(r1.isGp()).isTrue();
        assertThat(r1.getGpPatientsCount()).isEqualTo(2);
        assertThat(r1.getSpecialization()).extracting(SpecializationDtoResponse::getType).isEqualTo("Cardiology");

        // d2
        DoctorDataDtoResponse r2 = out.stream().filter(r -> r.getId().equals(11L)).findFirst().orElseThrow();
        assertThat(r2.getUserId()).isEqualTo(101L);
        assertThat(r2.isGp()).isTrue();
        assertThat(r2.getGpPatientsCount()).isZero();
        assertThat(r2.getSpecialization()).isNull();
    }

    @Test
    @DisplayName("showAllDoctors: maps all doctors to DTOs")
    void showAllDoctors_maps() {
        Doctor d1 = new Doctor(); d1.setId(1L); d1.setGp(false);
        User u1 = new User(); u1.setId(10L); d1.setUser(u1);

        Doctor d2 = new Doctor(); d2.setId(2L); d2.setGp(true);
        User u2 = new User(); u2.setId(20L); d2.setUser(u2);

        when(doctorRepository.findAll()).thenReturn(List.of(d1, d2));

        Set<DoctorDataDtoResponse> out = service.showAllDoctors();
        assertThat(out).extracting(DoctorDataDtoResponse::getId).containsExactlyInAnyOrder(1L, 2L);
        assertThat(out.stream().filter(r -> r.getId() == 2L).findFirst().orElseThrow().isGp()).isTrue();
    }

    @Test
    @DisplayName("showAllDoctorsWithSpecialization: filters by specialization type (ignore case) and maps")
    void showAllDoctorsWithSpecialization_maps() {
        Doctor d = new Doctor();
        d.setId(5L);
        User u = new User(); u.setId(50L); d.setUser(u);
        Specialization s = new Specialization(); s.setId(9L); s.setType("Neurology");
        d.setSpecialization(s);

        when(doctorRepository.findBySpecialization_TypeIgnoreCase("neurology"))
                .thenReturn(List.of(d));

        Set<DoctorDataDtoResponse> out = service.showAllDoctorsWithSpecialization("neurology");
        assertThat(out).hasSize(1);
        DoctorDataDtoResponse dto = out.iterator().next();
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getUserId()).isEqualTo(50L);
        assertThat(dto.getSpecialization()).isNotNull();
        assertThat(dto.getSpecialization().getType()).isEqualTo("Neurology");
    }

    @Test
    @DisplayName("showAllDoctorsWithMostSickLeavesGiven: uses counts order, maps via findAll() index")
    void showMostSickLeavesGiven_mapsAndOrders() {
        DoctorRepository.DoctorSickLeaveCount c1 = new DoctorRepository.DoctorSickLeaveCount() {
            @Override
            public Long getDoctorId() { return 2L; }

            @Override
            public Long getCount() { return 12L; }
        };
        DoctorRepository.DoctorSickLeaveCount c2 = new DoctorRepository.DoctorSickLeaveCount() {
            @Override
            public Long getDoctorId() { return 1L; }

            @Override
            public Long getCount() { return 9L; }
        };
        when(doctorRepository.countSickLeavesPerDoctor()).thenReturn(List.of(c1, c2));

        Doctor d1 = new Doctor(); d1.setId(1L); User u1 = new User(); u1.setId(101L); d1.setUser(u1);
        Doctor d2 = new Doctor(); d2.setId(2L); User u2 = new User(); u2.setId(102L); d2.setUser(u2);
        when(doctorRepository.findAll()).thenReturn(List.of(d1, d2));

        Set<DoctorDataDtoResponse> out = service.showAllDoctorsWithMostSickLeavesGiven();

        assertThat(out).extracting(DoctorDataDtoResponse::getId).containsExactly(2L, 1L);
        DoctorDataDtoResponse first = out.iterator().next();
        assertThat(first.getUserId()).isEqualTo(102L);
    }
}
