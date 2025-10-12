package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.SickLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SickLeaveRepository extends JpaRepository<SickLeave, Long> {

    List<SickLeave> findByDoctor_IdOrderByFromDateDesc(Long doctorId);

    List<SickLeave> findByPatient_IdOrderByFromDateDesc(Long patientId);

    // Month with most sick leaves (by fromDate’s month)
    interface MonthCount {

        Integer getM();

        long getC();
    }

    @Query("""
       select MONTH(s.fromDate) as m, count(s) as c
       from SickLeave s
       group by MONTH(s.fromDate)
       order by c desc
    """)
    List<MonthCount> countByMonthDesc();
}
