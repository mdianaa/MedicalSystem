package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "diagnoses")
public class Diagnosis extends BaseEntity {

    @ManyToOne
    private Doctor doctor; // name

    @Column(columnDefinition = "TEXT")
    private String complaints;

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;

    @ManyToMany
    private Set<Allergy> allergies;

    @OneToOne
    private SickLeave sickLeave;

    @OneToOne
    private Treatment treatment;
}
