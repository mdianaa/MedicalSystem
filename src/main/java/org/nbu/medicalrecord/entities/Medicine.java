package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nbu.medicalrecord.enums.MedicineType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "medicines")
public class Medicine extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "milligrams", nullable = false)
    private int mg;

    @Enumerated(EnumType.STRING)
    private MedicineType medicineType;
}
