package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.InviteToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InviteTokenRepository extends JpaRepository<InviteToken, Long> {
    Optional<InviteToken> findByValue(String value);
}
