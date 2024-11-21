package br.com.fiap.energyapi.domain.report;

import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysis;
import br.com.fiap.energyapi.domain.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne
    @JoinColumn(name = "device_analysis_id")
    @JsonIgnoreProperties({"user", "devices"})
    private DeviceAnalysis deviceAnalysis;

    @Column(name = "generated_at", nullable = false)
    private String generatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"devices", "energyMeters", "reports"})
    private User user;

}
