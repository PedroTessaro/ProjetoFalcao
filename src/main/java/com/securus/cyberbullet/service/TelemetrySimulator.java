package com.securus.cyberbullet.service;

import com.securus.cyberbullet.document.TelemetryRecord;
import com.securus.cyberbullet.domain.Drone;
import com.securus.cyberbullet.domain.DroneStatus;
import com.securus.cyberbullet.repository.DroneRepository;
import com.securus.cyberbullet.repository.TelemetryRepository;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simulador de telemetria em tempo real.
 *
 * <p>========================================================================
 * Este componente SIMULA os drones reais transmitindo telemetria para a
 * central. Em producao, NAO existiria: a central apenas RECEBERIA os dados
 * enviados pelos drones (via {@code CommunicationService}). Aqui geramos os
 * dados periodicamente para alimentar o dashboard e exercitar a navegacao.
 * ========================================================================
 *
 * <p>A cada ciclo, para cada drone EM VOO: le sensores + navega (pode gerar
 * evento de ameaca), atualiza o estado do drone no banco relacional e grava
 * uma leitura de telemetria no MongoDB.
 */
@Service
public class TelemetrySimulator {

    private static final Logger log = LoggerFactory.getLogger(TelemetrySimulator.class);

    private final DroneRepository droneRepository;
    private final TelemetryRepository telemetryRepository;
    private final NavigationService navigation;

    public TelemetrySimulator(DroneRepository droneRepository,
                              TelemetryRepository telemetryRepository,
                              NavigationService navigation) {
        this.droneRepository = droneRepository;
        this.telemetryRepository = telemetryRepository;
        this.navigation = navigation;
    }

    @Scheduled(fixedDelayString = "${cyberbullet.telemetry.interval-ms:2500}", initialDelay = 5000)
    @Transactional
    public void tick() {
        List<Drone> flying = droneRepository.findByStatus(DroneStatus.IN_FLIGHT);
        if (flying.isEmpty()) {
            return;
        }
        for (Drone drone : flying) {
            // [SIMULACAO DRONE] >>> Em producao estes valores chegariam do drone
            // pelo enlace de comunicacao; aqui sao gerados sinteticamente.
            double lidar = navigation.senseAndNavigate(drone);

            drone.setBatteryPct(Math.max(0, drone.getBatteryPct() - ThreadLocalRandom.current().nextDouble(0.2, 1.2)));
            drone.setLatitude(drone.getLatitude() + ThreadLocalRandom.current().nextDouble(-0.0008, 0.0008));
            drone.setLongitude(drone.getLongitude() + ThreadLocalRandom.current().nextDouble(-0.0008, 0.0008));
            drone.setAltitude(Math.max(0, drone.getAltitude() + ThreadLocalRandom.current().nextDouble(-5, 5)));
            drone.setLastContact(Instant.now());

            // Bateria critica -> retorna a base automaticamente (operacao autonoma).
            if (drone.getBatteryPct() < 15 && drone.getStatus() == DroneStatus.IN_FLIGHT) {
                log.warn("[AUTO][{}] Bateria critica ({}%). Retornando a base automaticamente.",
                        drone.getCodename(), String.format("%.0f", drone.getBatteryPct()));
                drone.setStatus(DroneStatus.RETURNING);
            }
            droneRepository.save(drone);

            TelemetryRecord t = new TelemetryRecord();
            t.setDroneId(drone.getId());
            t.setDroneCodename(drone.getCodename());
            t.setTimestamp(Instant.now());
            t.setBatteryPct(round(drone.getBatteryPct()));
            t.setLatitude(drone.getLatitude());
            t.setLongitude(drone.getLongitude());
            t.setAltitude(round(drone.getAltitude()));
            t.setSpeedKmh(round(ThreadLocalRandom.current().nextDouble(0, 60)));
            t.setHeadingDeg(round(ThreadLocalRandom.current().nextDouble(0, 360)));
            t.setLidarNearestObstacleM(round(lidar));
            t.setSignalStrength(round(ThreadLocalRandom.current().nextDouble(40, 100)));
            telemetryRepository.save(t);
        }
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
