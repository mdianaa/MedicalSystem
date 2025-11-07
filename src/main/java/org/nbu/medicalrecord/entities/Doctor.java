package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "doctors")
public class Doctor extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToMany
    @JoinTable(
            name = "doctor_specializations",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id"),
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"doctor_id", "specialization_id"})
            }
    )
    private Set<Specialization> specializations = new HashSet<>();

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean gp;

    @OneToMany(mappedBy = "gp")
    private Set<Patient> gpPatients;
}
