package com.securus.cyberbullet.service;

import com.securus.cyberbullet.domain.Mission;
import com.securus.cyberbullet.domain.MissionPriority;
import com.securus.cyberbullet.domain.MissionStatus;
import com.securus.cyberbullet.repository.MissionRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Monitor do Sistema Operacional Embarcado (SIMULADO).
 *
 * <p>Resolve os requisitos de "Sistemas Operacionais e Concorrencia":
 * o SO de bordo gerencia varias threads (fusao de sensores, navegacao, IA,
 * comunicacao, telemetria) e PRIORIZA processos conforme a criticidade da
 * missao. Aqui simulamos a tabela de processos e o escalonamento por
 * prioridade. Em producao isso seria um RTOS (ex.: FreeRTOS/PREEMPT_RT) a
 * bordo do drone.
 */
@Service
public class EmbeddedOsMonitorService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddedOsMonitorService.class);

    private static final String[] PROCESSES = {
            "sensor_fusion", "navigation", "ai_inference", "comms", "telemetry", "os_watchdog"
    };

    private final MissionRepository missionRepository;

    public EmbeddedOsMonitorService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    /**
     * Calcula a prioridade base de escalonamento a partir da missao ativa mais
     * critica. Quanto mais critica a missao, maior a fatia de CPU para os
     * processos de seguranca de voo (navegacao, IA).
     */
    public int currentSchedulingPriority() {
        return missionRepository.findByStatus(MissionStatus.IN_PROGRESS).stream()
                .map(Mission::getPriority)
                .mapToInt(MissionPriority::getWeight)
                .max()
                .orElse(MissionPriority.NORMAL.getWeight());
    }

    /** Snapshot da tabela de processos do SO embarcado (para o dashboard). */
    public List<ProcessInfo> processTable() {
        int basePriority = currentSchedulingPriority();
        List<ProcessInfo> table = new ArrayList<>();
        for (String name : PROCESSES) {
            int prio = priorityFor(name, basePriority);
            double cpu = ThreadLocalRandom.current().nextDouble(2.0, 18.0) + prio;
            double mem = ThreadLocalRandom.current().nextDouble(20.0, 120.0);
            String state = "os_watchdog".equals(name) ? "RUNNING" : pickState();
            table.add(new ProcessInfo(name, prio, round(cpu), round(mem), state));
        }
        table.sort(Comparator.comparingInt(ProcessInfo::priority).reversed());
        return table;
    }

    /**
     * Loga periodicamente o estado do SO embarcado e detecta processos
     * travados (watchdog). Em producao seria o telemetry do RTOS.
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 8000)
    public void monitor() {
        int prio = currentSchedulingPriority();
        // [SIMULACAO SO EMBARCADO] >>> Aqui o watchdog leria /proc do RTOS de bordo.
        log.info("[OS-MONITOR] Watchdog OK | prioridade de escalonamento={} | threads ativas: {}",
                prio, PROCESSES.length);
    }

    private int priorityFor(String process, int base) {
        // Navegacao, IA e fusao de sensores recebem prioridade elevada quando a
        // missao e critica - sao essenciais para a seguranca de voo.
        return switch (process) {
            case "navigation", "ai_inference", "sensor_fusion" -> Math.min(20, base + 8);
            case "comms" -> Math.min(20, base + 4);
            case "os_watchdog" -> 20;
            default -> base;
        };
    }

    private String pickState() {
        double r = ThreadLocalRandom.current().nextDouble();
        if (r > 0.92) {
            return "WAITING";
        }
        return "RUNNING";
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    /** Linha da tabela de processos do SO embarcado. */
    public record ProcessInfo(String name, int priority, double cpuPct, double memMb, String state) {
    }
}
