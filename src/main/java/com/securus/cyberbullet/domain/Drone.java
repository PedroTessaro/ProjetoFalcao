package com.securus.cyberbullet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

/**
 * Drone da frota (entidade relacional / H2).
 *
 * <p>Mantem o "ultimo estado conhecido" do drone. A telemetria de alta
 * frequencia (posicao instantanea, leituras de sensores) NAO fica aqui: vai
 * para o MongoDB (ver {@code TelemetryRecord}). O campo {@link #version}
 * (optimistic locking) garante a integridade/sincronizacao do estado quando
 * varios processos atualizam o mesmo drone.
 */
@Entity
@Table(name = "drones")
public class Drone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codename;

    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DroneStatus status = DroneStatus.IDLE;

    private double batteryPct = 100.0;

    private double latitude;
    private double longitude;
    private double altitude;

    private String firmwareVersion;

    private Instant lastContact;

    /** Optimistic locking: garante sincronizacao/integridade em concorrencia. */
    @Version
    private Long version;

    public Drone() {
    }

    public Drone(String codename, String model, String firmwareVersion) {
        this.codename = codename;
        this.model = model;
        this.firmwareVersion = firmwareVersion;
        this.lastContact = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodename() {
        return codename;
    }

    public void setCodename(String codename) {
        this.codename = codename;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public DroneStatus getStatus() {
        return status;
    }

    public void setStatus(DroneStatus status) {
        this.status = status;
    }

    public double getBatteryPct() {
        return batteryPct;
    }

    public void setBatteryPct(double batteryPct) {
        this.batteryPct = batteryPct;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Instant getLastContact() {
        return lastContact;
    }

    public void setLastContact(Instant lastContact) {
        this.lastContact = lastContact;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
