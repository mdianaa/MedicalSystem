package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
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

    @ManyToOne
    private Doctor doctor; // name

    @Column(name = "date_of_visit", nullable = false)
    private LocalDate dateOfVisit;

    @OneToOne
    private Diagnosis diagnosis;

    @ManyToOne(optional = true)
    private SickLeave sickLeave;  // no sick leave might be taken

}
