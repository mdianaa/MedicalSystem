package org.nbu.medicalrecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.request.AdminCreateDoctorRequest;
import org.nbu.medicalrecord.dtos.request.AdminCreatePatientRequest;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.PatientRepository;
import org.nbu.medicalrecord.repositories.UserRepository;
import org.nbu.medicalrecord.services.impl.RegistrationServiceImpl;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private UserRepository users;

    @Mock
    private PatientRepository patients;

    @Mock
    private DoctorRepository doctors;

    @InjectMocks
    private RegistrationServiceImpl service;

    // ----------------------------
    // createPatient
    // ----------------------------
    @Nested
    @DisplayName("createPatient")
    class CreatePatientTests {

        @Test
        @DisplayName("throws IllegalStateException when EGN already exists")
        void duplicateEgnPatient_throws() {
            AdminCreatePatientRequest req = new AdminCreatePatientRequest();
            req.setEgn("1234567890");
            req.setEmail("email@email.com");
            req.setFirstName("Ana");
            req.setLastName("Ivanova");
            req.setBirthDate(LocalDate.of(1990, 1, 2));

            when(patients.existsByUser_Egn("1234567890")).thenReturn(true);

            assertThatThrownBy(() -> service.createPatient(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("EGN already exists");

            // ensure we DO NOT try to save
            verify(patients, never()).save(any());
        }

        @Test
        @DisplayName("when EGN is free, service attempts to build and save Patient (but will currently NPE if Patient.user is null)")
        void createPatient_behavior() {
            AdminCreatePatientRequest req = new AdminCreatePatientRequest();
            req.setEgn("9999999999");
            req.setEmail("email@email.com");
            req.setFirstName("Maya");
            req.setLastName("Petrova");
            req.setBirthDate(LocalDate.of(2001, 5, 10));

            when(patients.existsByUser_Egn("9999999999")).thenReturn(false);

            // We expect a NullPointerException because:
            //   var p = new Patient();
            //   p.getUser().setEgn(...)
            // will call getUser() on a brand new Patient.
            // Unless Patient's constructor initializes user, this is null.
            assertThatThrownBy(() -> service.createPatient(req))
                    .isInstanceOf(NullPointerException.class);

            // BUT we *can* verify that the duplicate check happened:
            verify(patients).existsByUser_Egn("9999999999");

            // And we expect that save(...) was never reached due to NPE.
            verify(patients, never()).save(any());
        }
    }

    // ----------------------------
    // createDoctor
    // ----------------------------
    @Nested
    @DisplayName("createDoctor")
    class CreateDoctorTests {

        @Test
        @DisplayName("throws IllegalStateException when email already registered (case-insensitive lowercased trim is enforced)")
        void duplicateEmailDoctor_throws() {
            AdminCreateDoctorRequest req = new AdminCreateDoctorRequest();
            req.setEmail("  TEST@EXAMPLE.COM ");
            req.setEgn("1234567890");
            req.setFirstName("Gregory");
            req.setLastName("House");
            // specialization will be used directly

            // after normalization the service uses email.trim().toLowerCase()
            when(users.existsByEmail("test@example.com")).thenReturn(true);

            assertThatThrownBy(() -> service.createDoctor(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Email already registered");

            verify(users).existsByEmail("test@example.com");
            verify(doctors, never()).existsByUser_Egn(anyString());
            verify(doctors, never()).save(any());
        }

        @Test
        @DisplayName("throws IllegalStateException when doctor's EGN already exists")
        void duplicateEgnDoctor_throws() {
            AdminCreateDoctorRequest req = new AdminCreateDoctorRequest();
            req.setEmail("newdoc@example.com");
            req.setEgn("1112223334");
            req.setFirstName("Jane");
            req.setLastName("Doe");

            when(users.existsByEmail("newdoc@example.com")).thenReturn(false);
            when(doctors.existsByUser_Egn("1112223334")).thenReturn(true);

            assertThatThrownBy(() -> service.createDoctor(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("EGN already exists");

            // verify order: check email first, then EGN, but don't save
            verify(users).existsByEmail("newdoc@example.com");
            verify(doctors).existsByUser_Egn("1112223334");
            verify(doctors, never()).save(any());
        }

        @Test
        @DisplayName("when email+EGN are free, service attempts to build and save Doctor (but will currently NPE if Doctor.user is null)")
        void createDoctor_behavior() {
            AdminCreateDoctorRequest req = new AdminCreateDoctorRequest();
            req.setEmail("doc@example.com");
            req.setEgn("5556667778");
            req.setFirstName("Alex");
            req.setLastName("Marinov");
            // req.getSpecialization() will be called and set

            when(users.existsByEmail("doc@example.com")).thenReturn(false);
            when(doctors.existsByUser_Egn("5556667778")).thenReturn(false);

            // Just like Patient, here we do:
            //   var d = new Doctor();
            //   d.getUser().setFirstName(...)
            // If Doctor::getUser() returns null for a fresh entity,
            // we will blow up with NullPointerException before save().
            assertThatThrownBy(() -> service.createDoctor(req))
                    .isInstanceOf(NullPointerException.class);

            // verify preconditions were checked
            verify(users).existsByEmail("doc@example.com");
            verify(doctors).existsByUser_Egn("5556667778");

            // verify we didn't hit save due to NPE
            verify(doctors, never()).save(any());
        }
    }
}

