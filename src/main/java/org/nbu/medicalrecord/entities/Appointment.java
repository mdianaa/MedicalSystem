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
@Table(
        name = "appointments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"date", "hour_of_visit", "doctors_schedule_id"})
)
public class Appointment extends BaseEntity {

    // the provided entity is required to be used for the doctors' schedule, not for the patients
    // the doctors keep track of their available hours

    // час при лекар

    @Column
    @NotNull
    private LocalDate date;

    @Column(name = "hour_of_visit")
    @NotNull
    private LocalTime hourOfAppointment;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient; // nullable because not all slots might be booked, if null - appointment hour is available

    @ManyToOne
    private Doctor doctor;

}
