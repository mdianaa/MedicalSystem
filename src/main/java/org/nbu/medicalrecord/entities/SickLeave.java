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
@Table(name = "sick_leaves")
public class SickLeave extends BaseEntity {

    @Column(name = "from_date")
    @NotNull
    private LocalDate fromDate;

    @Column(name = "to_date")
    @NotNull
    private LocalDate toDate;

    @Column()
    @NotBlank
    @Lob
    private String reason;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    @NotNull
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @NotNull
    private Doctor doctor;
}
