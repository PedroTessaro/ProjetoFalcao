package com.securus.cyberbullet.domain;

/**
 * Prioridade da missao. Usada pelo monitor do SO embarcado para escalonar os
 * processos de bordo (sensores, navegacao, IA) conforme a criticidade.
 */
public enum MissionPriority {
    LOW(1),
    NORMAL(5),
    HIGH(8),
    CRITICAL(10);

    /** Peso usado no escalonamento de processos do SO embarcado simulado. */
    private final int weight;

    MissionPriority(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
