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

import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @Column(name = "personal_id", nullable = false, unique = true, length = 10)
    @Size(min = 10, max = 10, message = "Personal ID must be exactly 10 digits.")
    @Pattern(regexp = "\\d{10}", message = "Personal ID must contain only digits.")
    private String personalId;

    @Column(name = "first_name")
    @NotBlank
    @Length(max = 30)
    private String firstName;

    @Column(name = "last_name")
    @NotBlank
    @Length(max = 30)
    private String lastName;

    @Column(name = "birth_date")
    @NotNull
    private LocalDate birthDate;

    @OneToMany(mappedBy = "patient", targetEntity = HealthInsurance.class)
    private Set<HealthInsurance> healthInsurances;

    @OneToOne(mappedBy = "patient", targetEntity = MedicalRecord.class, cascade = CascadeType.ALL)
    private MedicalRecord medicalRecord;

    @ManyToOne
    @NotNull
    private Doctor GP;
}
