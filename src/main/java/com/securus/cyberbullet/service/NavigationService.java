package com.securus.cyberbullet.service;

import com.securus.cyberbullet.document.ThreatEvent;
import com.securus.cyberbullet.domain.Drone;
import com.securus.cyberbullet.repository.ThreatEventRepository;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Sistema de Navegacao Inteligente (SIMULADO).
 *
 * <p>========================================================================
 * FRONTEIRA DE HARDWARE / FIRMWARE
 * ========================================================================
 * Em um sistema real, esta classe NAO existiria na central: ela rodaria
 * EMBARCADA no drone, lendo sensores fisicos e executando uma rede neural de
 * navegacao. Aqui ela e SIMULADA para demonstrar o fluxo. Os pontos onde
 * entraria o hardware real estao marcados com "[SIMULACAO ...]".
 * ========================================================================
 */
@Service
public class NavigationService {

    private static final Logger log = LoggerFactory.getLogger(NavigationService.class);

    /** Abaixo desta distancia (m) o LIDAR considera o obstaculo uma ameaca. */
    private static final double THREAT_DISTANCE_M = 15.0;

    private final ThreatEventRepository threatRepository;
    private final AuditService audit;

    public NavigationService(ThreatEventRepository threatRepository, AuditService audit) {
        this.threatRepository = threatRepository;
        this.audit = audit;
    }

    /**
     * Le os sensores do drone (LIDAR/camera/GPS) e roda a rede neural de
     * navegacao. Se detectar ameaca, decide e registra uma manobra de evasao.
     *
     * @return a leitura do LIDAR (distancia do obstaculo mais proximo, em m).
     */
    public double senseAndNavigate(Drone drone) {
        // [SIMULACAO LIDAR] >>> Aqui ficaria a leitura do driver do sensor LIDAR
        // (ex.: Velodyne/Ouster via UDP). Substituir pela nuvem de pontos real.
        double lidarNearest = ThreadLocalRandom.current().nextDouble(3.0, 120.0);

        // [SIMULACAO CAMERA] >>> Aqui entraria o frame da camera + visao
        // computacional (deteccao de objetos/segmentacao).
        // [SIMULACAO GPS] >>> Aqui entraria a fixacao de posicao do receptor GNSS.
        log.debug("[NAV][{}] Sensores -> LIDAR={}m | camera=OK | GPS=fix-3D",
                drone.getCodename(), String.format("%.1f", lidarNearest));

        // [SIMULACAO REDE NEURAL] >>> Aqui rodaria a inferencia do modelo de
        // navegacao autonoma (ex.: TensorRT/ONNX) decidindo a trajetoria.
        if (lidarNearest < THREAT_DISTANCE_M) {
            handleThreat(drone, lidarNearest);
        }
        return lidarNearest;
    }

    private void handleThreat(Drone drone, double distance) {
        String[] actions = {"ASCEND_20M", "REROUTE_LEFT", "REROUTE_RIGHT", "HOVER_AND_HOLD", "EMERGENCY_DESCEND"};
        String action = actions[ThreadLocalRandom.current().nextInt(actions.length)];
        int severity = (int) Math.max(1, Math.min(10, Math.round(10 - (distance / 2))));

        log.warn("[NAV][{}] !!! AMEACA detectada a {}m -> rede neural decidiu: {} (severidade {})",
                drone.getCodename(), String.format("%.1f", distance), action, severity);

        ThreatEvent event = new ThreatEvent(drone.getId(), drone.getCodename(),
                "OBSTACLE", severity, action, distance);
        threatRepository.save(event);

        audit.record("SYSTEM", "NAVIGATION", "THREAT_EVASION",
                String.format("Drone %s evadiu obstaculo a %.1fm com manobra %s (sev %d)",
                        drone.getCodename(), distance, action, severity));
    }

    public Optional<ThreatEvent> lastThreat(Long droneId) {
        return threatRepository.findTop30ByOrderByDetectedAtDesc().stream()
                .filter(t -> t.getDroneId().equals(droneId))
                .findFirst();
    }
}
