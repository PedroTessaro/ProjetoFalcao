package com.securus.cyberbullet.repository;

import com.securus.cyberbullet.document.TelemetryRecord;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Repositorio NoSQL de telemetria em tempo real. */
public interface TelemetryRepository extends MongoRepository<TelemetryRecord, String> {

    List<TelemetryRecord> findTop20ByDroneIdOrderByTimestampDesc(Long droneId);

    TelemetryRecord findFirstByDroneIdOrderByTimestampDesc(Long droneId);
}
