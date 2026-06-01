package com.securus.cyberbullet.repository;

import com.securus.cyberbullet.domain.Drone;
import com.securus.cyberbullet.domain.DroneStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio relacional da frota de drones. */
public interface DroneRepository extends JpaRepository<Drone, Long> {
    List<Drone> findByStatus(DroneStatus status);

    long countByStatus(DroneStatus status);
}
