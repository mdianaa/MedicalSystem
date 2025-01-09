package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nbu.medicalrecord.enums.MedicineType;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "medication")
public class Medication extends BaseEntity {

    @ManyToMany
    private Set<Medicine> medicines;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prescription; // time of period to be taken, how often and dosage
}
