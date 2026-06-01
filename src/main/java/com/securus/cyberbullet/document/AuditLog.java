package com.securus.cyberbullet.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Registro de auditoria IMUTAVEL (documento NoSQL / MongoDB).
 *
 * <p>Atende ao requisito "armazenamento de logs de auditoria deve ser imutavel
 * e altamente disponivel". A imutabilidade e garantida por uma CADEIA DE HASH
 * (estilo blockchain): cada registro inclui o hash do registro anterior, de
 * modo que qualquer alteracao em um registro antigo invalida toda a cadeia
 * seguinte. O campo {@link #signature} simula uma assinatura digital do
 * registro. A alta disponibilidade viria da replicacao do MongoDB (replica
 * set / sharding).
 */
@Document(collection = "audit_log")
public class AuditLog {

    @Id
    private String id;

    /** Posicao sequencial na cadeia (0 = bloco genesis). */
    @Indexed(unique = true)
    private long sequence;

    @Indexed
    private Instant timestamp;

    /** Quem executou a acao (operador ou "SYSTEM"). */
    private String actor;

    /** Categoria: AUTH, FLEET, MISSION, NAVIGATION, COMMUNICATION, OS, SECURITY. */
    @Indexed
    private String category;

    private String action;
    private String details;

    /** Hash do registro anterior (encadeamento). */
    private String previousHash;

    /** SHA-256 deste registro (inclui previousHash). */
    private String hash;

    /** Assinatura digital simulada do registro. */
    private String signature;

    public AuditLog() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
