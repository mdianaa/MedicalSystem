package org.nbu.medicalrecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.request.AllergyDtoRequest;
import org.nbu.medicalrecord.dtos.response.AllergyDtoResponse;
import org.nbu.medicalrecord.entities.Allergy;
import org.nbu.medicalrecord.enums.AllergyType;
import org.nbu.medicalrecord.repositories.AllergyRepository;
import org.nbu.medicalrecord.services.impl.AllergyServiceImpl;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllergyServiceImplTest {

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private AllergyServiceImpl service;

    @Nested
    @DisplayName("addAllergy")
    class AddAllergyTests {

        @Test
        @DisplayName("saves new allergy with normalized allergen and returns DTO with diagnosesCount=0")
        void addNewAllergy() {
            AllergyDtoRequest req = new AllergyDtoRequest();
            req.setAllergen("  PoLlEn  "); // will be normalized to "pollen"
            req.setAllergyType(AllergyType.POLLEN);

            when(allergyRepository.existsByAllergenIgnoreCase("pollen")).thenReturn(false);
            doAnswer(inv -> {
                Allergy a = inv.getArgument(0, Allergy.class);
                a.setId(42L);
                return null;
            }).when(allergyRepository).save(any(Allergy.class));

            AllergyDtoResponse out = service.addAllergy(req);

            verify(allergyRepository).existsByAllergenIgnoreCase("pollen");

            ArgumentCaptor<Allergy> captor = ArgumentCaptor.forClass(Allergy.class);
            verify(allergyRepository).save(captor.capture());
            Allergy saved = captor.getValue();
            assertThat(saved.getAllergen()).isEqualTo("pollen");
            assertThat(saved.getAllergyType()).isEqualTo(AllergyType.POLLEN);
            assertThat(saved.getDiagnoses()).isNull();

            assertThat(out.getId()).isEqualTo(42L);
            assertThat(out.getAllergen()).isEqualTo("pollen");
            assertThat(out.getAllergyType()).isEqualTo(AllergyType.POLLEN);
            assertThat(out.getDiagnosesCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("throws IllegalStateException when allergy already exists (case-insensitive)")
        void addDuplicateAllergy_throws() {
            AllergyDtoRequest req = new AllergyDtoRequest();
            req.setAllergen("  MILK ");
            req.setAllergyType(AllergyType.FOOD);

            when(allergyRepository.existsByAllergenIgnoreCase("milk")).thenReturn(true);

            assertThatThrownBy(() -> service.addAllergy(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Allergy already exists");
            verify(allergyRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("showAllergy")
    class ShowAllergyTests {

        @Test
        @DisplayName("returns DTO with diagnosesCount=0 when diagnoses is null")
        void showAllergyWithNullDiagnosesReturnsCount0() {
            Allergy a = new Allergy();
            a.setId(7L);
            a.setAllergen("pollen");
            a.setAllergyType(AllergyType.POLLEN);
            a.setDiagnoses(null);

            when(allergyRepository.findWithDiagnosesByAllergenIgnoreCase("pollen"))
                    .thenReturn(Optional.of(a));

            AllergyDtoResponse out = service.showAllergy("  PoLlEn ");
            assertThat(out.getId()).isEqualTo(7L);
            assertThat(out.getAllergen()).isEqualTo("pollen");
            assertThat(out.getAllergyType()).isEqualTo(AllergyType.POLLEN);
            assertThat(out.getDiagnosesCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("returns DTO with diagnosesCount=size when diagnoses is non-empty")
        void showAllergyWithDiagnosesCountsSize() {
            Allergy a = new Allergy();
            a.setId(8L);
            a.setAllergen("milk");
            a.setAllergyType(AllergyType.FOOD);
            // we don't need full Diagnosis objects; only the size matters
            a.setDiagnoses(Set.of(new org.nbu.medicalrecord.entities.Diagnosis(),
                    new org.nbu.medicalrecord.entities.Diagnosis()));

            when(allergyRepository.findWithDiagnosesByAllergenIgnoreCase("milk"))
                    .thenReturn(Optional.of(a));

            AllergyDtoResponse out = service.showAllergy("MiLk");
            assertThat(out.getDiagnosesCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when allergy not found")
        void showAllergyNotFound_throws() {
            when(allergyRepository.findWithDiagnosesByAllergenIgnoreCase("unknown"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.showAllergy("  unknown "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Allergy not found");
        }
    }

    @Nested
    @DisplayName("deleteAllergy")
    class DeleteAllergyTests {

        @Test
        @DisplayName("deletes when allergy exists and has no diagnoses")
        void deleteAllergySuccessfully() {
            Allergy a = new Allergy();
            a.setId(9L);
            a.setAllergen("pollen");
            a.setDiagnoses(null); // no references

            when(allergyRepository.findWithDiagnosesByAllergenIgnoreCase("pollen"))
                    .thenReturn(Optional.of(a));

            service.deleteAllergy("  POLLEN ");

            verify(allergyRepository).delete(a);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when allergy not found")
        void deleteAllergyNotFound_throws() {
            when(allergyRepository.findWithDiagnosesByAllergenIgnoreCase("missing"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteAllergy(" missing "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Allergy not found");
            verify(allergyRepository, never()).delete(any());
        }

        @Test
        @DisplayName("throws IllegalStateException when allergy has diagnoses (is referenced)")
        void deleteAllergyReferenced_throws() {
            Allergy a = new Allergy();
            a.setId(10L);
            a.setAllergen("milk");
            a.setDiagnoses(Set.of(new org.nbu.medicalrecord.entities.Diagnosis()));

            when(allergyRepository.findWithDiagnosesByAllergenIgnoreCase("milk"))
                    .thenReturn(Optional.of(a));

            assertThatThrownBy(() -> service.deleteAllergy("MiLk"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot delete allergy");
            verify(allergyRepository, never()).delete(any());
        }
    }
}
