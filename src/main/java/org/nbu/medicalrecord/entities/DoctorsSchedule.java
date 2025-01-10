package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nbu.medicalrecord.enums.ShiftType;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "doctors_schedule")
public class DoctorsSchedule extends BaseEntity {

    @OneToOne(mappedBy = "doctorsSchedule", targetEntity = Doctor.class)
    @NotNull
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ShiftType shift;

    @OneToMany(mappedBy = "schedule", targetEntity = Appointment.class, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Appointment> appointments;
}
