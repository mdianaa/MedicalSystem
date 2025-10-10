package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Specialization> findByNameIgnoreCase(String name);

    List<Specialization> findAllByOrderByNameAsc();
}
