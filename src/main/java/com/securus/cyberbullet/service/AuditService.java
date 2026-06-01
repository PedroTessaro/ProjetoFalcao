package com.securus.cyberbullet.service;

import com.securus.cyberbullet.document.AuditLog;
import com.securus.cyberbullet.repository.AuditLogRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Trilha de auditoria IMUTAVEL com cadeia de hash (estilo blockchain).
 *
 * <p>Cada evento critico vira um {@link AuditLog} encadeado ao anterior pelo
 * hash. Adulterar qualquer registro antigo quebra a verificacao de toda a
 * cadeia a partir dele - e isso e detectado por {@link #verifyChain()}.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository repository;
    private final CryptoService crypto;

    public AuditService(AuditLogRepository repository, CryptoService crypto) {
        this.repository = repository;
        this.crypto = crypto;
    }

    /**
     * Acrescenta um registro imutavel a trilha. {@code synchronized} para
     * garantir a ordem/integridade da cadeia sob concorrencia.
     */
    public synchronized AuditLog record(String actor, String category, String action, String details) {
        AuditLog last = repository.findFirstByOrderBySequenceDesc();
        long seq = (last == null) ? 0 : last.getSequence() + 1;
        String prevHash = (last == null) ? "GENESIS" : last.getHash();

        AuditLog entry = new AuditLog();
        entry.setSequence(seq);
        // Trunca para milissegundos: o MongoDB so guarda essa precisao, entao
        // o hash calculado aqui precisa bater com o do registro relido.
        entry.setTimestamp(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        entry.setActor(actor);
        entry.setCategory(category);
        entry.setAction(action);
        entry.setDetails(details);
        entry.setPreviousHash(prevHash);

        String payload = seq + "|" + entry.getTimestamp() + "|" + actor + "|" + category
                + "|" + action + "|" + details + "|" + prevHash;
        entry.setHash(crypto.sha256(payload));
        entry.setSignature(crypto.sign(entry.getHash()));

        AuditLog saved = repository.save(entry);
        log.info("[AUDITORIA] #{} {} :: {} - {} (hash={}...)",
                seq, category, action, actor, saved.getHash().substring(0, 12));
        return saved;
    }

    /** Verifica a integridade de toda a cadeia de auditoria. */
    public ChainVerification verifyChain() {
        List<AuditLog> all = repository.findAllByOrderBySequenceAsc();
        String prevHash = "GENESIS";
        for (AuditLog e : all) {
            String payload = e.getSequence() + "|" + e.getTimestamp() + "|" + e.getActor()
                    + "|" + e.getCategory() + "|" + e.getAction() + "|" + e.getDetails()
                    + "|" + prevHash;
            String expectedHash = crypto.sha256(payload);
            boolean linkOk = e.getPreviousHash().equals(prevHash);
            boolean hashOk = e.getHash().equals(expectedHash);
            boolean sigOk = crypto.verify(e.getHash(), e.getSignature());
            if (!linkOk || !hashOk || !sigOk) {
                return new ChainVerification(false, all.size(), e.getSequence());
            }
            prevHash = e.getHash();
        }
        return new ChainVerification(true, all.size(), -1);
    }

    public List<AuditLog> latest() {
        return repository.findTop100ByOrderBySequenceDesc();
    }

    /** Resultado da verificacao de integridade da cadeia. */
    public record ChainVerification(boolean valid, long totalRecords, long firstBrokenSequence) {
    }
}
