package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    List<Diagnosis> findByPatient_Id(Long patientId);

    List<Diagnosis> findByDoctor_Id(Long doctorId);

    interface DiagnosisResultCount {
        String diagnosisResult();
        long cnt();
    }

    @Query("""
     select d.diagnosisResult as diagnosisResult, count(d) as cnt
     from Diagnosis d
     group by d.diagnosisResult
     order by count(d) desc
  """)
    List<DiagnosisResultCount> countByDiagnosisResultDesc();
}
