package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.SickLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SickLeaveRepository extends JpaRepository<SickLeave, Long> {
}
