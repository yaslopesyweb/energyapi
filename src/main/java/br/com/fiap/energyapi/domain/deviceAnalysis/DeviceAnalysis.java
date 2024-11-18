package br.com.fiap.energyapi.domain.deviceAnalysis;

import br.com.fiap.energyapi.domain.device.Device;
import br.com.fiap.energyapi.domain.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "device_analysis")
public class DeviceAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_analysis_id")
    private Long deviceAnalysisId;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    @JsonIgnoreProperties({"user", "energyMeters"})
    private Device device;

    @Column(name = "device_current_watts", nullable = false)
    private int deviceCurrentWatts;

    @Column(name = "energy_usage_monthly", nullable = false)
    private double energyUsageMonthly;

    @Column(name = "efficiency_class")
    private String efficiencyClass;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"devices", "energyMeters"})
    private User user;
}
