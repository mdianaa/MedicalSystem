package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Month;
import java.time.Year;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(
        name = "health_insurances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"patient_id", "month", "year"})
)
public class HealthInsurance extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column
    private Month month;

    @NotBlank
    @Column()
    private int year;

    @NotNull
    @Column(name = "is_paid", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isPaid;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
}
