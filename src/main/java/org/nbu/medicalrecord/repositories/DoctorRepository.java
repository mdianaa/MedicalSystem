package org.nbu.medicalrecord.repositories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.nbu.medicalrecord.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    boolean existsByUser_Egn(String egn);

    boolean existsBySpecialization_Id(Long specializationId);

    List<Doctor> findByGpTrue();

    Doctor findByUser_Email(String email);

    List<Doctor> findBySpecialization_TypeIgnoreCase(String type);

    interface DoctorSickLeaveCount {
        Long getDoctorId();
    }

    @Query("""
    select sl.doctor.id as doctorId, count(sl) as count
    from SickLeave sl
    group by sl.doctor.id
    order by count(sl) desc
  """)
    List<DoctorSickLeaveCount> countSickLeavesPerDoctor();
}
