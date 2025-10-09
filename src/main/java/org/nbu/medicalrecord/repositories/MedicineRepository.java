package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findByNameIgnoreCaseAndMg(String name, int mg);

    boolean existsByNameIgnoreCaseAndMg(String name, int mg);
}
