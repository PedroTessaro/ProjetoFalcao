package com.securus.cyberbullet.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Ameaca detectada pelo sistema de navegacao inteligente (documento NoSQL).
 *
 * <p>Gerada quando os sensores simulados (LIDAR/camera) identificam um
 * obstaculo/ameaca e a rede neural simulada decide uma manobra de evasao.
 */
@Document(collection = "threat_events")
public class ThreatEvent {

    @Id
    private String id;

    @Indexed
    private Long droneId;

    private String droneCodename;

    @Indexed
    private Instant detectedAt;

    /** Tipo da ameaca: OBSTACLE, JAMMING, COLLISION_RISK, GEOFENCE... */
    private String type;

    /** Severidade 1-10 inferida pela rede neural simulada. */
    private int severity;

    /** Manobra de evasao decidida (ex.: "ASCEND_20M", "REROUTE_LEFT"). */
    private String evasiveAction;

    private double distanceM;

    public ThreatEvent() {
    }

    public ThreatEvent(Long droneId, String droneCodename, String type, int severity,
                       String evasiveAction, double distanceM) {
        this.droneId = droneId;
        this.droneCodename = droneCodename;
        this.type = type;
        this.severity = severity;
        this.evasiveAction = evasiveAction;
        this.distanceM = distanceM;
        this.detectedAt = Instant.now();
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

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(Instant detectedAt) {
        this.detectedAt = detectedAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getEvasiveAction() {
        return evasiveAction;
    }

    public void setEvasiveAction(String evasiveAction) {
        this.evasiveAction = evasiveAction;
    }

    public double getDistanceM() {
        return distanceM;
    }

    public void setDistanceM(double distanceM) {
        this.distanceM = distanceM;
    }
}
