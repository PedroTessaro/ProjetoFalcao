package com.securus.cyberbullet.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Leitura de telemetria de um drone em tempo real (documento NoSQL / MongoDB).
 *
 * <p>Dados de alta frequencia e alto volume - exatamente o caso de uso de um
 * banco NoSQL distribuido. Cada documento e uma "foto" do drone num instante,
 * derivada dos sensores simulados (LIDAR/camera/GPS).
 */
@Document(collection = "telemetry")
public class TelemetryRecord {

    @Id
    private String id;

    @Indexed
    private Long droneId;

    private String droneCodename;

    @Indexed
    private Instant timestamp;

    private double batteryPct;
    private double latitude;
    private double longitude;
    private double altitude;
    private double speedKmh;
    private double headingDeg;

    /** Distancia (m) do obstaculo mais proximo lido pelo LIDAR simulado. */
    private double lidarNearestObstacleM;

    /** Forca do sinal de comunicacao (0-100). */
    private double signalStrength;

    public TelemetryRecord() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getDroneId() {
        return droneId;
    }

    public void setDroneId(Long droneId) {
        this.droneId = droneId;
    }

    public String getDroneCodename() {
        return droneCodename;
    }

    public void setDroneCodename(String droneCodename) {
        this.droneCodename = droneCodename;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
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

    public double getSpeedKmh() {
        return speedKmh;
    }

    public void setSpeedKmh(double speedKmh) {
        this.speedKmh = speedKmh;
    }

    public double getHeadingDeg() {
        return headingDeg;
    }

    public void setHeadingDeg(double headingDeg) {
        this.headingDeg = headingDeg;
    }

    public double getLidarNearestObstacleM() {
        return lidarNearestObstacleM;
    }

    public void setLidarNearestObstacleM(double lidarNearestObstacleM) {
        this.lidarNearestObstacleM = lidarNearestObstacleM;
    }

    public double getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(double signalStrength) {
        this.signalStrength = signalStrength;
    }
}
