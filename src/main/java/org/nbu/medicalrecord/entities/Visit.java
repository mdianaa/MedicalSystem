package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "visits")
public class Visit extends BaseEntity {

    // преглед

    @OneToOne
    @JoinColumn(name = "appointment_id")
    @NotNull
    private Appointment appointment;

    @ManyToOne
    private Doctor doctor;

    @OneToOne(cascade = CascadeType.ALL)
    private Diagnosis diagnosis;

    @ManyToOne
    private SickLeave sickLeave;  // no sick leave might be taken

    @ManyToOne
    @JoinColumn(name = "medical_record_id")
    @NotNull
    private MedicalRecord medicalRecord;
}
