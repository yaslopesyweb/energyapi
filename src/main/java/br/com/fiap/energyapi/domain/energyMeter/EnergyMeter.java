package br.com.fiap.energyapi.domain.energyMeter;

import br.com.fiap.energyapi.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
@Table(name = "energy_meters")
public class EnergyMeter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "energy_meter_id")
    private Long energyMeterId;

    @Column(name = "meter_name", nullable = false)
    private String meterName;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"energyMeters"})
    private User user;
}
