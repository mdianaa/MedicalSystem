package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne
    private Specialization specialization;

    @OneToOne(mappedBy = "doctor", fetch = FetchType.LAZY, optional = false)
    private DoctorsSchedule doctorsSchedule;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    @NotNull
    private boolean gp;

    @OneToMany(mappedBy = "gp")
    private Set<Patient> gpPatients;
}
