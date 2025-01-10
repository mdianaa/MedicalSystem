package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    @Column()
    @NotBlank
    @Lob
    private String complaints;

    @Column(name = "medical_history")
    @Lob
    private String medicalHistory;  // patient's past conditions relevant to the current diagnosis

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<Allergy> allergies;

    @Column(name = "diagnosis_result")
    @NotBlank
    @Lob
    private String diagnosisResult;

    @OneToOne(cascade = CascadeType.ALL)
    private Medication medication;   // no medications might be prescribed

    @Column(name = "required_tests")
    @Lob
    private String requiredTests;   // lab tests if needed
}
