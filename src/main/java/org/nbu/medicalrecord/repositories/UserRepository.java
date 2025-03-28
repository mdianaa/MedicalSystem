package org.nbu.medicalrecord.repositories;

import org.nbu.medicalrecord.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEgn(String egn);
}
