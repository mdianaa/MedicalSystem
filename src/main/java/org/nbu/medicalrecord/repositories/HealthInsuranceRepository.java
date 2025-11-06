package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.HealthInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Month;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthInsuranceRepository extends JpaRepository<HealthInsurance, Long> {

    boolean existsByPatient_IdAndMonthAndYear(Long patientId, Month month, int year);

    Optional<HealthInsurance> findByPatient_IdAndMonthAndYear(Long patientId, Month month, int year);

    List<HealthInsurance> findByPatient_IdAndYearOrderByMonthAsc(Long patientId, int year);

    // Last 6 months for a patient, crossing year boundaries (requires numeric year)
    @Query("""
      select hi from HealthInsurance hi
      where hi.patient.id = :patientId
      order by hi.year desc, hi.month desc
    """)
    List<HealthInsurance> findAllByPatientOrderByYearMonthDesc(Long patientId);

    // ALL for a patient
    @Query("""
        select hi from HealthInsurance hi
        where hi.patient.id = :patientId
        order by hi.year desc,
        case hi.month
            when 'JANUARY' then 1  when 'FEBRUARY' then 2  when 'MARCH' then 3
            when 'APRIL' then 4    when 'MAY' then 5      when 'JUNE' then 6
            when 'JULY' then 7     when 'AUGUST' then 8   when 'SEPTEMBER' then 9
            when 'OCTOBER' then 10 when 'NOVEMBER' then 11 when 'DECEMBER' then 12
        end desc
    """)
    List<HealthInsurance> findAllByPatientOrderByYearCalendarMonthDesc(Long patientId);

    // PAID only
    @Query("""
        select hi from HealthInsurance hi
        where hi.patient.id = :patientId and hi.isPaid = true
        order by hi.year desc,
        case hi.month
            when 'JANUARY' then 1  when 'FEBRUARY' then 2  when 'MARCH' then 3
            when 'APRIL' then 4    when 'MAY' then 5      when 'JUNE' then 6
            when 'JULY' then 7     when 'AUGUST' then 8   when 'SEPTEMBER' then 9
            when 'OCTOBER' then 10 when 'NOVEMBER' then 11 when 'DECEMBER' then 12
        end desc
    """)
    List<HealthInsurance> findPaidByPatientOrderByYearMonthDesc(Long patientId);

    // UNPAID only
    @Query("""
        select hi from HealthInsurance hi
        where hi.patient.id = :patientId and hi.isPaid = false
        order by hi.year desc,
        case hi.month
            when 'JANUARY' then 1  when 'FEBRUARY' then 2  when 'MARCH' then 3
            when 'APRIL' then 4    when 'MAY' then 5      when 'JUNE' then 6
            when 'JULY' then 7     when 'AUGUST' then 8   when 'SEPTEMBER' then 9
            when 'OCTOBER' then 10 when 'NOVEMBER' then 11 when 'DECEMBER' then 12
        end desc
    """)
    List<HealthInsurance> findUnpaidByPatientOrderByYearMonthDesc(Long patientId);
}
