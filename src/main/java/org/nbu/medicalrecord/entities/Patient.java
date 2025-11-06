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

import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "patients")
public class Patient extends BaseEntity{

    @Column(name = "birth_date")
    @NotNull
    private LocalDate birthDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<Allergy> allergies;  // patient might not have any allergies

    @OneToMany(mappedBy = "patient", targetEntity = HealthInsurance.class)
    private Set<HealthInsurance> healthInsurances;

    @OneToOne(mappedBy = "patient", targetEntity = MedicalRecord.class, cascade = CascadeType.ALL)
    private MedicalRecord medicalRecord;

    @ManyToOne
    @JoinColumn(name = "gp_doctor_id")
    private Doctor gp;
}
