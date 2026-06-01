package com.securus.cyberbullet.repository;

import com.securus.cyberbullet.document.ThreatEvent;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Repositorio NoSQL de eventos de ameaca. */
public interface ThreatEventRepository extends MongoRepository<ThreatEvent, String> {
    List<ThreatEvent> findTop30ByOrderByDetectedAtDesc();
}
