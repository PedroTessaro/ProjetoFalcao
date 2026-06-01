package com.securus.cyberbullet.controller;

import com.securus.cyberbullet.document.TelemetryRecord;
import com.securus.cyberbullet.domain.Drone;
import com.securus.cyberbullet.repository.TelemetryRepository;
import com.securus.cyberbullet.repository.ThreatEventRepository;
import com.securus.cyberbullet.service.DroneService;
import com.securus.cyberbullet.service.EmbeddedOsMonitorService;
import com.securus.cyberbullet.service.MissionService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST consumida pelo dashboard (polling em JS) para atualizar a
 * telemetria em tempo real sem recarregar a pagina.
 */
@RestController
@RequestMapping("/api")
public class DashboardApiController {

    private final DroneService droneService;
    private final MissionService missionService;
    private final TelemetryRepository telemetryRepository;
    private final ThreatEventRepository threatRepository;
    private final EmbeddedOsMonitorService osMonitor;

    public DashboardApiController(DroneService droneService, MissionService missionService,
                                  TelemetryRepository telemetryRepository,
                                  ThreatEventRepository threatRepository,
                                  EmbeddedOsMonitorService osMonitor) {
        this.droneService = droneService;
        this.missionService = missionService;
        this.telemetryRepository = telemetryRepository;
        this.threatRepository = threatRepository;
        this.osMonitor = osMonitor;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("fleet", droneService.stats());
        out.put("missions", missionService.stats());
        out.put("schedulingPriority", osMonitor.currentSchedulingPriority());

        List<Map<String, Object>> live = new ArrayList<>();
        for (Drone drone : droneService.findAll()) {
            TelemetryRecord t = telemetryRepository.findFirstByDroneIdOrderByTimestampDesc(drone.getId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", drone.getId());
            row.put("codename", drone.getCodename());
            row.put("status", drone.getStatus());
            row.put("battery", Math.round(drone.getBatteryPct()));
            if (t != null) {
                row.put("lat", t.getLatitude());
                row.put("lon", t.getLongitude());
                row.put("alt", t.getAltitude());
                row.put("speed", t.getSpeedKmh());
                row.put("lidar", t.getLidarNearestObstacleM());
                row.put("signal", t.getSignalStrength());
            }
            live.add(row);
        }
        out.put("drones", live);
        out.put("threats", threatRepository.findTop30ByOrderByDetectedAtDesc());
        out.put("processes", osMonitor.processTable());
        return out;
    }

    @GetMapping("/telemetry/{droneId}")
    public List<TelemetryRecord> telemetry(@org.springframework.web.bind.annotation.PathVariable Long droneId) {
        return telemetryRepository.findTop20ByDroneIdOrderByTimestampDesc(droneId);
    }
}
