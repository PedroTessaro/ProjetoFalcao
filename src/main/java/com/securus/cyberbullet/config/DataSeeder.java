package com.securus.cyberbullet.config;

import com.securus.cyberbullet.domain.Drone;
import com.securus.cyberbullet.domain.DroneStatus;
import com.securus.cyberbullet.domain.Operator;
import com.securus.cyberbullet.domain.Role;
import com.securus.cyberbullet.repository.DroneRepository;
import com.securus.cyberbullet.repository.OperatorRepository;
import com.securus.cyberbullet.service.AuditService;
import com.securus.cyberbullet.service.TotpService;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Popula dados iniciais (operadores + frota) na primeira execucao. */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final OperatorRepository operatorRepository;
    private final DroneRepository droneRepository;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totp;
    private final AuditService audit;

    public DataSeeder(OperatorRepository operatorRepository, DroneRepository droneRepository,
                      PasswordEncoder passwordEncoder, TotpService totp, AuditService audit) {
        this.operatorRepository = operatorRepository;
        this.droneRepository = droneRepository;
        this.passwordEncoder = passwordEncoder;
        this.totp = totp;
        this.audit = audit;
    }

    @Override
    public void run(String... args) {
        if (operatorRepository.count() == 0) {
            seedOperator("admin", "admin123", "Administrador Securus", Role.ADMIN);
            seedOperator("operador", "operador123", "Operador de Drones", Role.OPERATOR);
            seedOperator("viewer", "viewer123", "Analista (Somente Leitura)", Role.VIEWER);
            log.info("============================================================");
            log.info(" OPERADORES CRIADOS (usuario / senha):");
            log.info("   admin    / admin123     (ADMIN)");
            log.info("   operador / operador123  (OPERATOR)");
            log.info("   viewer   / viewer123    (VIEWER)");
            log.info(" Todos exigem MFA/TOTP. O codigo valido e mostrado na tela");
            log.info(" de MFA (modo demonstracao) e tambem aqui no log.");
            log.info("============================================================");
        }

        if (droneRepository.count() == 0) {
            createDrone("FALCAO-01", "Securus Raptor X", DroneStatus.IN_FLIGHT);
            createDrone("FALCAO-02", "Securus Raptor X", DroneStatus.IN_FLIGHT);
            createDrone("FALCAO-03", "Securus Condor", DroneStatus.IDLE);
            createDrone("FALCAO-04", "Securus Condor", DroneStatus.IDLE);
            createDrone("FALCAO-05", "Securus Hawk Lite", DroneStatus.MAINTENANCE);
            createDrone("FALCAO-06", "Securus Hawk Lite", DroneStatus.IN_FLIGHT);
            audit.record("SYSTEM", "FLEET", "BOOTSTRAP", "Frota inicial de drones provisionada.");
        }
    }

    private void seedOperator(String username, String rawPassword, String fullName, Role role) {
        Operator op = new Operator(username, passwordEncoder.encode(rawPassword), fullName, role);
        op.setMfaEnabled(true);
        op.setTotpSecret(totp.newSecret());
        op.setBiometricId("BIO-" + username.toUpperCase());
        operatorRepository.save(op);
    }

    private void createDrone(String codename, String model, DroneStatus status) {
        Drone drone = new Drone(codename, model, "v2.4.1");
        drone.setStatus(status);
        drone.setBatteryPct(ThreadLocalRandom.current().nextDouble(45, 100));
        // Regiao de Sao Paulo (apenas para a simulacao do mapa/coordenadas).
        drone.setLatitude(-23.55 + ThreadLocalRandom.current().nextDouble(-0.1, 0.1));
        drone.setLongitude(-46.63 + ThreadLocalRandom.current().nextDouble(-0.1, 0.1));
        drone.setAltitude(ThreadLocalRandom.current().nextDouble(50, 200));
        droneRepository.save(drone);
    }
}
