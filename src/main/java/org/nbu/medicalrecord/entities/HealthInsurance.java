package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
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
    private Month month;

    @Column(name = "is_paid", nullable = false)
    private boolean isPaid;

    @ManyToOne
    private Patient patient;
}
