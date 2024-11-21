package br.com.fiap.energyapi.domain.device;

import br.com.fiap.energyapi.domain.energyMeter.EnergyMeter;
import br.com.fiap.energyapi.domain.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "device_name", nullable = false)
    private String deviceName;

    @Column(name = "device_type", nullable = false)
    private String deviceType;

    @ManyToOne
    @JoinColumn(name = "energy_meter_id", nullable = false)
    @JsonIgnoreProperties({"user", "devices"})
    private EnergyMeter energyMeter;

    @Column(name = "estimated_usage_hours")
    private String estimatedUsageHours;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"devices", "energyMeters"})
    private User user;
}