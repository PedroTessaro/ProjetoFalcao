package com.securus.cyberbullet.service;

import com.securus.cyberbullet.domain.Drone;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Gerenciamento de Comunicacao com os drones (SIMULADO).
 *
 * <p>Resolve os requisitos de "comunicacao segura e em tempo real" e
 * "mecanismos de fallback para evitar perda de conexao". O envio real de
 * comandos ao drone esta marcado com "[SIMULACAO ...]".
 */
@Service
public class CommunicationService {

    private static final Logger log = LoggerFactory.getLogger(CommunicationService.class);

    /** Probabilidade simulada de falha no canal primario. */
    private static final double PRIMARY_LINK_FAILURE_RATE = 0.12;

    private final AuditService audit;

    public CommunicationService(AuditService audit) {
        this.audit = audit;
    }

    /**
     * Envia um comando criptografado ao drone. Tenta o canal primario
     * (5G/RF) e, em caso de falha, aciona o canal de fallback (satelite),
     * evitando perda de conexao.
     *
     * @return true se o comando foi entregue (por qualquer canal).
     */
    public boolean sendSecureCommand(Drone drone, String command) {
        // [SIMULACAO CRIPTOGRAFIA] >>> Aqui o payload seria cifrado (AES-256-GCM)
        // e o canal seria TLS 1.3 mutuo (mTLS) com o drone.
        log.info("[COMM][{}] Cifrando comando '{}' (AES-256-GCM) e enviando via canal PRIMARIO (mTLS/5G)...",
                drone.getCodename(), command);

        // [SIMULACAO RADIO/REDE] >>> Aqui ficaria o transporte real (socket/MAVLink).
        boolean primaryOk = ThreadLocalRandom.current().nextDouble() > PRIMARY_LINK_FAILURE_RATE;
        if (primaryOk) {
            log.info("[COMM][{}] ACK recebido pelo canal primario.", drone.getCodename());
            return true;
        }

        // ---- FALLBACK ----
        log.warn("[COMM][{}] Canal primario FALHOU. Acionando FALLBACK (link de satelite)...",
                drone.getCodename());
        boolean fallbackOk = ThreadLocalRandom.current().nextDouble() > 0.05;
        if (fallbackOk) {
            log.info("[COMM][{}] Comando entregue pelo canal de FALLBACK.", drone.getCodename());
            audit.record("SYSTEM", "COMMUNICATION", "FALLBACK_USED",
                    "Comando '" + command + "' entregue ao drone " + drone.getCodename()
                            + " via canal de fallback (satelite).");
            return true;
        }

        log.error("[COMM][{}] PERDA DE CONEXAO em ambos os canais para o comando '{}'.",
                drone.getCodename(), command);
        audit.record("SYSTEM", "COMMUNICATION", "LINK_LOST",
                "Perda total de conexao com o drone " + drone.getCodename()
                        + " ao enviar '" + command + "'.");
        return false;
    }

    /** Heartbeat periodico para checar a saude do enlace. */
    public boolean heartbeat(Drone drone) {
        // [SIMULACAO HEARTBEAT] >>> Aqui seria o keep-alive real com o drone.
        return ThreadLocalRandom.current().nextDouble() > 0.03;
    }
}
