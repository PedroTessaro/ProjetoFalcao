package com.securus.cyberbullet.service;

import com.securus.cyberbullet.domain.Drone;
import com.securus.cyberbullet.domain.DroneStatus;
import com.securus.cyberbullet.domain.Mission;
import com.securus.cyberbullet.domain.MissionStatus;
import com.securus.cyberbullet.repository.DroneRepository;
import com.securus.cyberbullet.repository.MissionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestao do ciclo de vida das missoes (banco relacional). */
@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final DroneRepository droneRepository;
    private final DroneService droneService;
    private final AuditService audit;

    public MissionService(MissionRepository missionRepository, DroneRepository droneRepository,
                          DroneService droneService, AuditService audit) {
        this.missionRepository = missionRepository;
        this.droneRepository = droneRepository;
        this.droneService = droneService;
        this.audit = audit;
    }

    public List<Mission> findAll() {
        return missionRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Mission> findById(Long id) {
        return missionRepository.findById(id);
    }

    @Transactional
    public Mission create(Mission mission, String actor) {
        mission.setStatus(MissionStatus.PLANNED);
        mission.setCreatedBy(actor);
        mission.setCreatedAt(Instant.now());
        Mission saved = missionRepository.save(mission);
        audit.record(actor, "MISSION", "CREATED",
                "Missao '" + saved.getName() + "' (prioridade " + saved.getPriority() + ") criada.");
        return saved;
    }

    @Transactional
    public Mission start(Long missionId, String actor) {
        Mission mission = require(missionId);
        Drone drone = mission.getDrone();
        if (drone == null) {
            throw new IllegalStateException("Missao sem drone atribuido.");
        }
        mission.setStatus(MissionStatus.IN_PROGRESS);
        mission.setStartedAt(Instant.now());
        missionRepository.save(mission);

        droneService.sendCommand(drone.getId(), "LAUNCH", actor);

        audit.record(actor, "MISSION", "STARTED",
                "Missao '" + mission.getName() + "' iniciada com drone " + drone.getCodename());
        return mission;
    }

    @Transactional
    public Mission finish(Long missionId, MissionStatus outcome, String actor) {
        Mission mission = require(missionId);
        mission.setStatus(outcome);
        mission.setFinishedAt(Instant.now());
        missionRepository.save(mission);

        Drone drone = mission.getDrone();
        if (drone != null) {
            droneService.sendCommand(drone.getId(), "RECALL", actor);
            drone.setStatus(DroneStatus.IDLE);
            droneRepository.save(drone);
        }
        audit.record(actor, "MISSION", outcome.name(),
                "Missao '" + mission.getName() + "' finalizada com status " + outcome);
        return mission;
    }

    public MissionStats stats() {
        return new MissionStats(
                missionRepository.count(),
                missionRepository.countByStatus(MissionStatus.IN_PROGRESS),
                missionRepository.countByStatus(MissionStatus.COMPLETED),
                missionRepository.countByStatus(MissionStatus.PLANNED));
    }

    private Mission require(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Missao inexistente: " + id));
    }

    public record MissionStats(long total, long inProgress, long completed, long planned) {
    }
}
