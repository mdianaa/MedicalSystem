package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Month;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "health_insurances")
public class HealthInsurance extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @NotNull
    private Month month;

    @Column(name = "is_paid", columnDefinition = "BOOLEAN DEFAULT FALSE")
    @NotNull
    private boolean isPaid;

    @ManyToOne
    private Patient patient;
}
