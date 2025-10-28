package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appt_doctor_date_hour", columnList = "doctor_id,date,hour_of_visit", unique = true),
        @Index(name = "idx_appt_patient", columnList = "patient_id")
})
public class Appointment extends BaseEntity {

    // the patient creates an appointment for a particular doctor
    // the appointment is then related with the particular doctor's schedule

    // час при лекар

    @Column
    @NotNull
    private LocalDate date;

    @Column(name = "hour_of_visit")
    @NotNull
    private LocalTime hourOfAppointment;

    @ManyToOne @JoinColumn(name = "patient_id")
    private Patient patient;               // null => slot available (if you pre-create slots)

    @ManyToOne @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

}
