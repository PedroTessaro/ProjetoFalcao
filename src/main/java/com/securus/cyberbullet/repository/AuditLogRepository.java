package com.securus.cyberbullet.repository;

import com.securus.cyberbullet.document.AuditLog;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Repositorio NoSQL da trilha de auditoria imutavel (hash-chain). */
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    AuditLog findFirstByOrderBySequenceDesc();

    List<AuditLog> findTop100ByOrderBySequenceDesc();

    List<AuditLog> findAllByOrderBySequenceAsc();
}
