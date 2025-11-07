package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "diagnoses")
public class Diagnosis extends BaseEntity {

    @Column()
    @NotNull
    @Lob
    private String diagnosis;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Patient patient;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Doctor doctor;
}
