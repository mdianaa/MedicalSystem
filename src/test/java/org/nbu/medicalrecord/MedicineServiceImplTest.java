package org.nbu.medicalrecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.request.MedicineDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicineDtoResponse;
import org.nbu.medicalrecord.entities.Medicine;
import org.nbu.medicalrecord.enums.MedicineType;
import org.nbu.medicalrecord.repositories.MedicineRepository;
import org.nbu.medicalrecord.services.impl.MedicineServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineServiceImplTest {

    @Mock
    private MedicineRepository medicineRepository;

    @InjectMocks
    private MedicineServiceImpl service;

    private Medicine med(long id, String name, int mg, MedicineType type) {
        Medicine m = new Medicine();
        m.setId(id);
        m.setName(name);
        m.setMg(mg);
        m.setMedicineType(type);
        return m;
    }

    // -------- addMedicine --------
    @Nested
    @DisplayName("addMedicine")
    class AddMedicineTests {

        @Test
        @DisplayName("throws if medicine with same normalized name+mg already exists")
        void duplicateMedicine_throws() {
            MedicineDtoRequest req = new MedicineDtoRequest();
            req.setName("  Paracetamol ");
            req.setMg(500);
            req.setMedicineType(MedicineType.FEVER);

            // repo says there's already a match
            when(medicineRepository.existsByNameIgnoreCaseAndMg("Paracetamol", 500))
                    .thenReturn(true);

            assertThatThrownBy(() -> service.addMedicine(req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already exists");

            verify(medicineRepository, never()).save(any());
        }

        @Test
        @DisplayName("saves new Medicine and returns DTO (name trimmed, fields mapped)")
        void addMedicineSuccessfully() {
            MedicineDtoRequest req = new MedicineDtoRequest();
            req.setName("  Ibuprofen  ");
            req.setMg(200);
            req.setMedicineType(MedicineType.HEADACHE);

            when(medicineRepository.existsByNameIgnoreCaseAndMg("Ibuprofen", 200))
                    .thenReturn(false);

            // simulate JPA assigning ID on save
            when(medicineRepository.save(any(Medicine.class))).thenAnswer(inv -> {
                Medicine m = inv.getArgument(0);
                m.setId(42L);
                return m;
            });

            MedicineDtoResponse out = service.addMedicine(req);

            // verify what we saved
            ArgumentCaptor<Medicine> captor = ArgumentCaptor.forClass(Medicine.class);
            verify(medicineRepository).save(captor.capture());
            Medicine saved = captor.getValue();

            assertThat(saved.getName()).isEqualTo("Ibuprofen");
            assertThat(saved.getMg()).isEqualTo(200);
            assertThat(saved.getMedicineType()).isEqualTo(MedicineType.HEADACHE);

            // verify DTO mapping matches saved entity
            assertThat(out.getId()).isEqualTo(42L);
            assertThat(out.getName()).isEqualTo("Ibuprofen");
            assertThat(out.getMg()).isEqualTo(200);
            assertThat(out.getMedicineType()).isEqualTo(MedicineType.HEADACHE.toString());
        }
    }

    // -------- showMedicine --------
    @Nested
    @DisplayName("showMedicine")
    class ShowMedicineTests {

        @Test
        @DisplayName("throws if not found by (trimmed) name + mg")
        void notFoundMedicine_throws() {
            when(medicineRepository.findByNameIgnoreCaseAndMg("Aspirin", 100))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.showMedicine("   Aspirin   ", 100))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("returns DTO when found by (trimmed) name + mg")
        void foundReturnsMedicineDto() {
            Medicine entity = med(7L, "Aspirin", 100, MedicineType.COLD);
            when(medicineRepository.findByNameIgnoreCaseAndMg("ASPIRIN", 100))
                    .thenReturn(Optional.of(entity));

            MedicineDtoResponse dto = service.showMedicine("  ASPIRIN ", 100);

            assertThat(dto.getId()).isEqualTo(7L);
            assertThat(dto.getName()).isEqualTo("Aspirin");
            assertThat(dto.getMg()).isEqualTo(100);
            assertThat(dto.getMedicineType()).isEqualTo(MedicineType.COLD.toString());
        }
    }

    // -------- showAllMedicines --------
    @Test
    @DisplayName("showAllMedicines: maps all Medicine entities to DTOs")
    void showAllMedicines_maps() {
        Medicine m1 = med(1L, "Ibuprofen", 200, MedicineType.HEADACHE);
        Medicine m2 = med(2L, "Aspirin", 100, MedicineType.COLD);
        Medicine m3 = med(3L, "EyeDrops", 5, null); // null type should map to null type string

        when(medicineRepository.findAll()).thenReturn(List.of(m1, m2, m3));

        Set<MedicineDtoResponse> out = service.showAllMedicines();

        assertThat(out).hasSize(3);

        // check one with type
        assertThat(
                out.stream().anyMatch(d ->
                        d.getId().equals(1L)
                                && d.getName().equals("Ibuprofen")
                                && d.getMg() == 200
                                && d.getMedicineType().equals(MedicineType.HEADACHE.toString())
                )
        ).isTrue();

        // check one with null type
        assertThat(
                out.stream().anyMatch(d ->
                        d.getId().equals(3L)
                                && d.getName().equals("EyeDrops")
                                && d.getMg() == 5
                                && d.getMedicineType() == null
                )
        ).isTrue();
    }

    // -------- deleteMedicine --------
    @Nested
    @DisplayName("deleteMedicine")
    class DeleteMedicineTests {

        @Test
        @DisplayName("throws when medicine does not exist")
        void deleteMissing_throws() {
            when(medicineRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.deleteMedicine(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");

            verify(medicineRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("deletes when medicine exists")
        void deleteSuccessfully() {
            when(medicineRepository.existsById(5L)).thenReturn(true);

            service.deleteMedicine(5L);

            verify(medicineRepository).deleteById(5L);
        }
    }
}

