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
@Table(name = "medical_records")
public class MedicalRecord extends BaseEntity {

    @OneToOne
    private Patient patient; // name, personal id, date of birth

    @OneToMany
    private Set<Diagnosis> diagnoses;
}
