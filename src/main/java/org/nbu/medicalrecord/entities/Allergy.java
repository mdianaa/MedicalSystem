package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nbu.medicalrecord.enums.AllergyType;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "allergies")
public class Allergy extends BaseEntity {

    @Column()
    @NotBlank
    private String allergen;

    @Enumerated(EnumType.STRING)
    private AllergyType allergyType;

    @ManyToMany(mappedBy = "allergies")
    private Set<Diagnosis> diagnoses;
}
