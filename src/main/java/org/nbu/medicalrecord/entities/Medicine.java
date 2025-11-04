package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "medicines")
public class Medicine extends BaseEntity {

    @Column()
    @NotBlank
    @Length(max = 30)
    private String name;

    @Column(name = "milligrams")
    @NotNull
    @Positive
    @Min(1)
    private int mg;

    @Column()
    @NotBlank
    @Length(max = 30)
    private String medicineType;
}
