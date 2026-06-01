package com.securus.cyberbullet.service;

import com.securus.cyberbullet.domain.Drone;
import com.securus.cyberbullet.domain.DroneStatus;
import com.securus.cyberbullet.repository.DroneRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestao da frota de drones (banco relacional). */
@Service
public class DroneService {

    private final DroneRepository droneRepository;
    private final CommunicationService communication;
    private final AuditService audit;

    public DroneService(DroneRepository droneRepository, CommunicationService communication,
                        AuditService audit) {
        this.droneRepository = droneRepository;
        this.communication = communication;
        this.audit = audit;
    }

    public List<Drone> findAll() {
        return droneRepository.findAll();
    }

    public Optional<Drone> findById(Long id) {
        return droneRepository.findById(id);
    }

    public List<Drone> findInFlight() {
        return droneRepository.findByStatus(DroneStatus.IN_FLIGHT);
    }

    @Transactional
    public Drone save(Drone drone) {
        return droneRepository.save(drone);
    }

    /**
     * Envia um comando ao drone (controle remoto). O comando passa pela camada
     * de comunicacao segura com fallback e e auditado.
     */
    @Transactional
    public boolean sendCommand(Long droneId, String command, String actor) {
        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> new IllegalArgumentException("Drone inexistente: " + droneId));

        boolean delivered = communication.sendSecureCommand(drone, command);
        if (delivered) {
            switch (command) {
                case "LAUNCH" -> drone.setStatus(DroneStatus.IN_FLIGHT);
                case "RECALL" -> drone.setStatus(DroneStatus.RETURNING);
                case "LAND" -> drone.setStatus(DroneStatus.IDLE);
                default -> { /* outros comandos nao mudam o status */ }
            }
            drone.setLastContact(Instant.now());
        } else {
            drone.setStatus(DroneStatus.LINK_LOST);
        }
        droneRepository.save(drone);
        audit.record(actor, "FLEET", "COMMAND_" + command,
                "Comando " + command + " -> drone " + drone.getCodename()
                        + " | entregue=" + delivered);
        return delivered;
    }

    public FleetStats stats() {
        return new FleetStats(
                droneRepository.count(),
                droneRepository.countByStatus(DroneStatus.IN_FLIGHT),
                droneRepository.countByStatus(DroneStatus.IDLE),
                droneRepository.countByStatus(DroneStatus.LINK_LOST),
                droneRepository.countByStatus(DroneStatus.MAINTENANCE));
    }

    public record FleetStats(long total, long inFlight, long idle, long linkLost, long maintenance) {
    }
}
