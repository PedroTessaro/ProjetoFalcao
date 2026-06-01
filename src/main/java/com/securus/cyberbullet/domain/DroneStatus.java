package com.securus.cyberbullet.domain;

/** Estado operacional de um drone na frota. */
public enum DroneStatus {
    /** Em base, pronto para decolar. */
    IDLE,
    /** Em voo, executando missao. */
    IN_FLIGHT,
    /** Retornando para a base. */
    RETURNING,
    /** Comunicacao perdida (acionou fallback). */
    LINK_LOST,
    /** Em manutencao / indisponivel. */
    MAINTENANCE
}
