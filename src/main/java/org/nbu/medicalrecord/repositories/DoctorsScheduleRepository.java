package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.DoctorsSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorsScheduleRepository extends JpaRepository<DoctorsSchedule, Long> {

    Optional<DoctorsSchedule> findByDoctor_Id(Long doctorId);

    boolean existsByDoctor_Id(Long doctorId);
}
