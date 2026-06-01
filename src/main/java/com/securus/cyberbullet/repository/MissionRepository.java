package com.securus.cyberbullet.repository;

import com.securus.cyberbullet.domain.Mission;
import com.securus.cyberbullet.domain.MissionStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio relacional do historico de missoes. */
public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findAllByOrderByCreatedAtDesc();

    List<Mission> findByStatus(MissionStatus status);

    long countByStatus(MissionStatus status);
}
