package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.MedicineDtoRequest;
import org.nbu.medicalrecord.dtos.response.MedicineDtoResponse;
import org.nbu.medicalrecord.entities.Medicine;
import org.nbu.medicalrecord.enums.MedicineType;
import org.nbu.medicalrecord.repositories.MedicineRepository;
import org.nbu.medicalrecord.services.MedicineService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;

    @Override
    @Transactional
    public MedicineDtoResponse addMedicine(MedicineDtoRequest req) {
        String nameNorm = req.getName().trim();
        if (medicineRepository.existsByNameIgnoreCaseAndMg(nameNorm, req.getMg())) {
            throw new IllegalStateException("Medicine already exists (name+mg must be unique).");
        }
        Medicine m = new Medicine();
        m.setName(nameNorm);
        m.setMg(req.getMg());
        m.setMedicineType(req.getMedicineType());
        medicineRepository.save(m);
        return toDto(m);
    }

    @Override
    public MedicineDtoResponse showMedicine(String name, int mg) {
        Medicine m = medicineRepository.findByNameIgnoreCaseAndMg(name.trim(), mg)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));
        return toDto(m);
    }

    @Override
    public Set<MedicineDtoResponse> showAllMedicines() {
        return medicineRepository
                .findAll()
                .stream().
                map(this::toDto)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void deleteMedicine(long medicineId) {
        if (!medicineRepository.existsById(medicineId)) {
            throw new IllegalArgumentException("Medicine not found");
        }
        medicineRepository.deleteById(medicineId);
    }

    private MedicineDtoResponse toDto(Medicine m) {
        return new MedicineDtoResponse(
                m.getId(),
                m.getName(),
                m.getMg(),
                m.getMedicineType() != null ? m.getMedicineType().toString() : null
        );
    }
}