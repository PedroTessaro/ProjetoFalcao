package com.securus.cyberbullet.domain;

/** Perfis de acesso dos operadores da central de controle. */
public enum Role {
    /** Acesso total: gestao da frota, missoes e auditoria. */
    ADMIN,
    /** Operador de drones: opera frota e missoes. */
    OPERATOR,
    /** Apenas visualizacao do dashboard e auditoria. */
    VIEWER
}
