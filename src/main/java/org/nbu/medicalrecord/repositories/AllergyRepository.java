package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.Allergy;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long> {

    boolean existsByAllergenIgnoreCase(String allergen);

    Optional<Allergy> findByAllergenIgnoreCase(String allergen);

    long deleteByAllergenIgnoreCase(String allergen);

    @EntityGraph(attributePaths = {"diagnoses"})
    Optional<Allergy> findWithDiagnosesByAllergenIgnoreCase(String allergen);
}
