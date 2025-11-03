package org.nbu.medicalrecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.request.SpecializationDtoRequest;
import org.nbu.medicalrecord.dtos.response.SpecializationDtoResponse;
import org.nbu.medicalrecord.entities.Specialization;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.SpecializationRepository;
import org.nbu.medicalrecord.services.impl.SpecializationServiceImpl;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecializationServiceImplTest {

    @Mock
    private SpecializationRepository repo;

    @Mock
    private DoctorRepository doctorRepo;

    @InjectMocks
    private SpecializationServiceImpl service;

    private Specialization spec(long id, String type) {
        Specialization s = new Specialization();
        s.setId(id);
        s.setType(type);
        return s;
    }

    // ---------------------------
    // addNewSpecialization
    // ---------------------------
    @Nested
    @DisplayName("addNewSpecialization")
    class AddNewSpecializationTests {

        @Test
        @DisplayName("throws when specialization (normalized) already exists according to existsByTypeIgnoreCase")
        void existingSpecialization_throws() {
            SpecializationDtoRequest req = new SpecializationDtoRequest();
            req.setType("  Cardiology  ");

            when(repo.existsByTypeIgnoreCase("Cardiology")).thenReturn(true);

            assertThatThrownBy(() -> service.addNewSpecialization(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already exists")
                    .hasMessageContaining("Cardiology");

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("wraps DataIntegrityViolationException on save into IllegalStateException (race condition)")
        void raceCondition() {
            SpecializationDtoRequest req = new SpecializationDtoRequest();
            req.setType("Neurology");

            when(repo.existsByTypeIgnoreCase("Neurology")).thenReturn(false);
            when(repo.save(any(Specialization.class)))
                    .thenThrow(new DataIntegrityViolationException("unique violation"));

            assertThatThrownBy(() -> service.addNewSpecialization(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already exists")
                    .hasMessageContaining("Neurology");

            verify(repo).save(any(Specialization.class));
        }

        @Test
        @DisplayName("creates new Specialization, saves it, and returns DTO")
        void creteSpecializationSuccessfully() {
            SpecializationDtoRequest req = new SpecializationDtoRequest();
            req.setType("  Dermatology ");

            when(repo.existsByTypeIgnoreCase("Dermatology")).thenReturn(false);

            // simulate JPA assigning id
            when(repo.save(any(Specialization.class))).thenAnswer(inv -> {
                Specialization s = inv.getArgument(0);
                s.setId(42L);
                return s;
            });

            SpecializationDtoResponse out = service.addNewSpecialization(req);

            // verify what we saved to repo
            ArgumentCaptor<Specialization> captor = ArgumentCaptor.forClass(Specialization.class);
            verify(repo).save(captor.capture());
            Specialization saved = captor.getValue();

            assertThat(saved.getType()).isEqualTo("Dermatology");

            // verify DTO
            assertThat(out.getId()).isEqualTo(42L);
            assertThat(out.getType()).isEqualTo("Dermatology");
        }
    }

    // ---------------------------
    // showAllSpecializations
    // ---------------------------
    @Test
    @DisplayName("showAllSpecializations: maps all specs to DTOs preserving repo order (LinkedHashSet)")
    void showAllSpecializations_sortedAndMapped() {
        Specialization s1 = spec(1L, "Allergology");
        Specialization s2 = spec(2L, "Cardiology");
        Specialization s3 = spec(3L, "Dermatology");

        // Repo is supposed to already return type ASC
        when(repo.findAllByOrderByTypeAsc()).thenReturn(List.of(s1, s2, s3));

        Set<SpecializationDtoResponse> out = service.showAllSpecializations();

        verify(repo).findAllByOrderByTypeAsc();

        assertThat(out).hasSize(3);

        // check order by iteration
        assertThat(out.stream().map(SpecializationDtoResponse::getType).toList())
                .containsExactly("Allergology", "Cardiology", "Dermatology");

        // basic mapping correctness
        assertThat(out.stream().anyMatch(d -> d.getId().equals(2L) && d.getType().equals("Cardiology")))
                .isTrue();
    }

    // ---------------------------
    // deleteSpecialization
    // ---------------------------
    @Nested
    @DisplayName("deleteSpecialization")
    class DeleteSpecializationTests {

        @Test
        @DisplayName("throws when specialization does not exist")
        void specializationMissing_throws() {
            when(repo.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteSpecialization(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Specialization not found");

            verify(repo, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("throws when specialization is referenced by at least one doctor")
        void specializationInUse_throws() {
            when(repo.existsById(5L)).thenReturn(true);
            when(doctorRepo.existsBySpecialization_Id(5L)).thenReturn(true);

            assertThatThrownBy(() -> service.deleteSpecialization(5L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot delete specialization");

            verify(repo, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("deletes when specialization exists and is not in use")
        void deletesWhenSafeSuccessfully() {
            when(repo.existsById(7L)).thenReturn(true);
            when(doctorRepo.existsBySpecialization_Id(7L)).thenReturn(false);

            service.deleteSpecialization(7L);

            verify(repo).deleteById(7L);
        }
    }
}

