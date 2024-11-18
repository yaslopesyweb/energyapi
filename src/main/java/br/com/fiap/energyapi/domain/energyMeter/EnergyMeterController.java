package br.com.fiap.energyapi.domain.energyMeter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/energy-meters")
public class EnergyMeterController {

    private final EnergyMeterService energyMeterService;

    @Autowired
    public EnergyMeterController(EnergyMeterService energyMeterService) {
        this.energyMeterService = energyMeterService;
    }

    @PostMapping
    public ResponseEntity<EnergyMeter> createEnergyMeter(@RequestBody EnergyMeter energyMeter) {
        EnergyMeter createdEnergyMeter = energyMeterService.createEnergyMeter(energyMeter);
        return ResponseEntity.ok(createdEnergyMeter);
    }

    @GetMapping
    public ResponseEntity<List<EnergyMeter>> getAllEnergyMeters() {
        List<EnergyMeter> energyMeters = energyMeterService.getAllEnergyMeters();
        return ResponseEntity.ok(energyMeters);
    }

    @GetMapping("/{energyMeterId}")
    public ResponseEntity<EnergyMeter> getEnergyMeterById(@PathVariable Long energyMeterId) {
        return energyMeterService.getEnergyMeterById(energyMeterId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{energyMeterId}")
    public ResponseEntity<EnergyMeter> updateEnergyMeter(@PathVariable Long energyMeterId, @RequestBody EnergyMeter energyMeterDetails) {
        try {
            EnergyMeter updatedEnergyMeter = energyMeterService.updateEnergyMeter(energyMeterId, energyMeterDetails);
            return ResponseEntity.ok(updatedEnergyMeter);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{energyMeterId}")
    public ResponseEntity<Void> deleteEnergyMeter(@PathVariable Long energyMeterId) {
        energyMeterService.deleteEnergyMeter(energyMeterId);
        return ResponseEntity.noContent().build();
    }
}
