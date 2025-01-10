package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.DoctorsSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorsScheduleRepository extends JpaRepository<DoctorsSchedule, Long> {
}
