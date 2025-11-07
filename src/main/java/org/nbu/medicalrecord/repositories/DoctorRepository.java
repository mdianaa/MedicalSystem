package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    boolean existsByUser_Egn(String egn);

    boolean existsBySpecializations_Id(Long specializationId);

    List<Doctor> findByGpTrue();

    Optional<Doctor> findByUser_Email(String email);

    @Query("""
           select distinct d
           from Doctor d
           join d.specializations s
           where lower(s.type) = lower(:type)
           """)
    List<Doctor> findBySpecializationType(@Param("type") String type);

    interface DoctorSickLeaveCount {
        Long getDoctorId();
        Long getCount();
    }

    @Query("""
    select sl.doctor.id as doctorId, count(sl) as count
    from SickLeave sl
    group by sl.doctor.id
    order by count(sl) desc
  """)
    List<DoctorSickLeaveCount> countSickLeavesPerDoctor();
}
