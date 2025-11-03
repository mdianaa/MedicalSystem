package org.nbu.medicalrecord;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nbu.medicalrecord.dtos.request.MedicationDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicationDtoResponse;
import org.nbu.medicalrecord.dtos.response.MedicineDtoResponse;
import org.nbu.medicalrecord.entities.Diagnosis;
import org.nbu.medicalrecord.entities.Medication;
import org.nbu.medicalrecord.entities.Medicine;
import org.nbu.medicalrecord.enums.MedicineType;
import org.nbu.medicalrecord.repositories.DiagnosisRepository;
import org.nbu.medicalrecord.repositories.MedicationRepository;
import org.nbu.medicalrecord.repositories.MedicineRepository;
import org.nbu.medicalrecord.services.impl.MedicationServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationServiceImplTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private DiagnosisRepository diagnosisRepository;

    @InjectMocks
    private MedicationServiceImpl service;

    // helper to build Medicine
    private Medicine med(long id, String name, int mg, MedicineType type) {
        Medicine m = new Medicine();
        m.setId(id);
        m.setName(name);
        m.setMg(mg);
        m.setMedicineType(type);
        return m;
    }

    // ---------- addMedication ----------
    @Nested
    @DisplayName("addMedication")
    class AddMedicationTests {

        @Test
        @DisplayName("throws when some medicineIds do not exist")
        void missingMedicineIds_throws() {
            MedicationDtoRequest req = new MedicationDtoRequest();
            req.setMedicineIds(List.of(1L, 2L, 3L));
            req.setPrescription("Take daily");

            // repo only returns 2 medicines out of 3 requested
            Medicine m1 = med(1L, "Ibuprofen", 200, MedicineType.HEADACHE);
            Medicine m2 = med(2L, "Aspirin", 100, MedicineType.COLD);
            when(medicineRepository.findAllById(List.of(1L, 2L, 3L)))
                    .thenReturn(List.of(m1, m2));

            assertThatThrownBy(() -> service.addMedication(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Some medicineIds do not exist");

            verify(medicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("saves new Medication with all medicines and returns DTO (sorted meds, mapped fields)")
        void saveMedicationSuccessfully() {
            MedicationDtoRequest req = new MedicationDtoRequest();
            req.setMedicineIds(List.of(10L, 20L));
            req.setPrescription("1 tablet twice daily");

            // build medicines out of order on purpose to test sorting
            Medicine mA = med(10L, "Zithromax", 250, MedicineType.COUGH);
            Medicine mB = med(20L, "Amoxicillin", 500, MedicineType.SOUR_THROAT);

            when(medicineRepository.findAllById(List.of(10L, 20L)))
                    .thenReturn(List.of(mA, mB));

            // simulate JPA assigning ID on save
            when(medicationRepository.save(any(Medication.class))).thenAnswer(inv -> {
                Medication medEntity = inv.getArgument(0);
                medEntity.setId(99L);
                return medEntity;
            });

            MedicationDtoResponse out = service.addMedication(req);

            // verify repository received the Medication with correct data
            ArgumentCaptor<Medication> captor = ArgumentCaptor.forClass(Medication.class);
            verify(medicationRepository).save(captor.capture());
            Medication savedMedication = captor.getValue();
            assertThat(savedMedication.getPrescription()).isEqualTo("1 tablet twice daily");
            assertThat(savedMedication.getMedicines()).extracting(Medicine::getId)
                    .containsExactlyInAnyOrder(10L, 20L);

            // verify DTO mapping
            assertThat(out.getId()).isEqualTo(99L);
            assertThat(out.getPrescription()).isEqualTo("1 tablet twice daily");

            // medicines in DTO should be sorted by name THEN mg ascending
            List<String> dtoMedsOrdered = out.getMedicines().stream()
                    .map(m -> m.getName() + ":" + m.getMg())
                    .toList();

            assertThat(dtoMedsOrdered)
                    .containsExactly(
                            "Amoxicillin:500", // 'A' comes before 'Z'
                            "Zithromax:250"
                    );

            Map<String, String> dtoNameToType = out.getMedicines().stream()
                    .collect(Collectors.toMap(MedicineDtoResponse::getName, MedicineDtoResponse::getMedicineType));
            assertThat(dtoNameToType.get("Zithromax")).isEqualTo(MedicineType.COUGH.toString());
            assertThat(dtoNameToType.get("Amoxicillin")).isEqualTo(MedicineType.SOUR_THROAT.toString());
        }
    }

    // ---------- showAllMedications ----------
    @Test
    @DisplayName("showAllMedications: maps every Medication to MedicationDtoResponse")
    void showAllMedications_maps() {
        Medication med1 = new Medication();
        med1.setId(1L);
        med1.setPrescription("P1");
        med1.setMedicines(new HashSet<>(Set.of(
                med(10L, "Ibuprofen", 200, MedicineType.HEADACHE)
        )));

        Medication med2 = new Medication();
        med2.setId(2L);
        med2.setPrescription("P2");
        med2.setMedicines(new HashSet<>(Set.of(
                med(20L, "Aspirin", 100, MedicineType.COLD),
                med(21L, "Aspirin", 300, MedicineType.COLD)
        )));

        when(medicationRepository.findAll()).thenReturn(List.of(med1, med2));

        Set<MedicationDtoResponse> out = service.showAllMedications();

        assertThat(out).hasSize(2);

        // assert one of them
        assertThat(out.stream().anyMatch(dto ->
                dto.getId().equals(1L)
                        && dto.getPrescription().equals("P1")
                        && dto.getMedicines().size() == 1
        )).isTrue();

        // sorted-by-name-then-mg rule should apply inside each dto
        MedicationDtoResponse dto2 = out.stream()
                .filter(dto -> dto.getId().equals(2L))
                .findFirst()
                .orElseThrow();

        List<String> dto2Order = dto2.getMedicines().stream()
                .map(m -> m.getName() + ":" + m.getMg())
                .toList();

        assertThat(dto2Order)
                .containsExactly(
                        "Aspirin:100",
                        "Aspirin:300"
                );
    }

    // ---------- showAllMedicationsByDoctor ----------
    @Test
    @DisplayName("showAllMedicationsByDoctor: collects medications from diagnoses of that doctor, skipping null, preserving insertion order")
    void showAllMedicationsByDoctor_collectsFromDiagnoses() {
        // Build Medication A
        Medication medicationA = new Medication();
        medicationA.setId(100L);
        medicationA.setPrescription("rest + fluids");
        medicationA.setMedicines(new HashSet<>(Set.of(
                med(1L, "Ibuprofen", 200, MedicineType.HEADACHE)
        )));

        // Build Medication B
        Medication medicationB = new Medication();
        medicationB.setId(200L);
        medicationB.setPrescription("antibiotics");
        medicationB.setMedicines(new HashSet<>(Set.of(
                med(2L, "Amoxicillin", 500, MedicineType.SOUR_THROAT)
        )));

        // Diagnosis 1 -> medicationA
        Diagnosis d1 = new Diagnosis();
        d1.setMedication(medicationA);

        // Diagnosis 2 -> no medication (should be filtered out)
        Diagnosis d2 = new Diagnosis();
        d2.setMedication(null);

        // Diagnosis 3 -> medicationB
        Diagnosis d3 = new Diagnosis();
        d3.setMedication(medicationB);

        when(diagnosisRepository.findByDoctor_Id(77L))
                .thenReturn(List.of(d1, d2, d3));

        Set<MedicationDtoResponse> out = service.showAllMedicationsByDoctor(77L);

        assertThat(out).hasSize(2);

        Iterator<MedicationDtoResponse> it = out.iterator();
        MedicationDtoResponse first = it.next();
        MedicationDtoResponse second = it.next();

        // Order should match encounter order of non-null meds: medicationA then medicationB
        assertThat(first.getId()).isEqualTo(100L);
        assertThat(first.getPrescription()).isEqualTo("rest + fluids");

        assertThat(second.getId()).isEqualTo(200L);
        assertThat(second.getPrescription()).isEqualTo("antibiotics");

        // And check internal medicine mapping for one of them
        assertThat(first.getMedicines()).hasSize(1);
        MedicineDtoResponse onlyMed = first.getMedicines().getFirst();
        assertThat(onlyMed.getName()).isEqualTo("Ibuprofen");
        assertThat(onlyMed.getMg()).isEqualTo(200);
    }
}

