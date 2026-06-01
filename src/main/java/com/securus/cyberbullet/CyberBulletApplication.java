package com.securus.cyberbullet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Cyber Bullet System - Central de Controle de Frotas de Drones (Securus Dynamics).
 *
 * <p>Aplicacao Spring Boot que implementa a CENTRAL DE CONTROLE. Os drones, o
 * hardware embarcado (LIDAR, cameras, GPS) e o sistema operacional de bordo NAO
 * sao implementados aqui - eles sao SIMULADOS pelas classes do pacote
 * {@code service} (procure por blocos marcados com "[SIMULACAO ...]"), que
 * indicam exatamente onde ficaria a integracao real com o firmware do drone.
 */
@SpringBootApplication
@EnableScheduling
public class CyberBulletApplication {

    public static void main(String[] args) {
        SpringApplication.run(CyberBulletApplication.class, args);
    }
}
