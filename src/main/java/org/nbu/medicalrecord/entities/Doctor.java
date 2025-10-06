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

    @Column(name = "first_name")
    @NotBlank
    @Length(max = 30)
    private String firstName;

    @Column(name = "last_name")
    @NotBlank
    @Length(max = 30)
    private String lastName;

    @Column(name = "egn", unique = true, length = 10)
    @NotBlank
    @Size(min = 10, max = 10, message = "Personal ID must be exactly 10 digits.")
    @Pattern(regexp = "\\d{10}", message = "Personal ID must contain only digits.")
    private String egn;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

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
