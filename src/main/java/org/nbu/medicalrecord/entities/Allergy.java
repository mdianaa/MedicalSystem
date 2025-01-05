package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nbu.medicalrecord.enums.AllergyName;
import org.nbu.medicalrecord.enums.AllergyType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "allergies")
public class Allergy extends BaseEntity{

    @Enumerated(EnumType.STRING)
    private AllergyType allergyType;

    @Enumerated(EnumType.STRING)
    private AllergyName allergen;
}
