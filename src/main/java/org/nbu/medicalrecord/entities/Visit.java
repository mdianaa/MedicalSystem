package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    @OneToOne(optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    @NotNull
    private Appointment appointment;

    @Column()
    @NotBlank
    @Lob
    private String complaints;

    @Column(name = "medical_history")
    @Lob
    private String medicalHistory;  // patient's past conditions relevant to the current diagnosis

    @OneToOne(cascade = CascadeType.ALL)
    private Diagnosis diagnosis;

    @OneToOne(cascade = CascadeType.ALL)
    private Medication medication;   // no medications might be prescribed

    @Column(name = "required_tests")
    @Lob
    private String requiredTests;   // lab tests if needed

    @ManyToOne
    private SickLeave sickLeave;  // no sick leave might be taken

    @ManyToOne
    @JoinColumn(name = "medical_record_id")
    @NotNull
    private MedicalRecord medicalRecord;
}
