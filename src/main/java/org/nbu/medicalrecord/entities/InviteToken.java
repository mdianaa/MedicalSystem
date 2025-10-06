package org.nbu.medicalrecord.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "invite_tokens", indexes = {
        @Index(name = "ix_invite_token_value", columnList = "value", unique = true)
})
@Data
public class InviteToken extends BaseEntity {

    // for portal activation

    @Column(nullable = false, unique = true, length = 64)
    private String value; // random url-safe token

    @Column(nullable = false)
    private String email; // target email (normalized)

    @Enumerated(EnumType.STRING)
    private SubjectType subjectType; // PATIENT or DOCTOR

    private Long subjectId; // patientId or doctorId

    private Instant expiresAt;
    private Instant usedAt;

    public boolean isActive() { return usedAt == null && Instant.now().isBefore(expiresAt); }

    public enum SubjectType { PATIENT, DOCTOR }
}