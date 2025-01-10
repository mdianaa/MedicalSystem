package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "doctors")
public class Doctor extends BaseEntity {

    @Column(name = "first_name")
    @NotBlank
    @Length(max = 30)
    private String firstName;

    @Column(name = "last_name")
    @NotBlank
    @Length(max = 30)
    private String lastName;

    @ManyToOne
    private Specialization specialization;

    @OneToOne
    @NotNull
    private DoctorsSchedule doctorsSchedule;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    @NotNull
    private boolean gp;

    @OneToMany(mappedBy = "GP")
    private Set<Patient> gpPatients;
}
