package com.securus.cyberbullet.repository;

import com.securus.cyberbullet.domain.Operator;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio relacional de operadores. */
public interface OperatorRepository extends JpaRepository<Operator, Long> {
    Optional<Operator> findByUsername(String username);
}
