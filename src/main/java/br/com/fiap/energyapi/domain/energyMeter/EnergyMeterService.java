package br.com.fiap.energyapi.domain.energyMeter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EnergyMeterService {

    private final EnergyMeterRepository energyMeterRepository;

    @Autowired
    public EnergyMeterService(EnergyMeterRepository energyMeterRepository) {
        this.energyMeterRepository = energyMeterRepository;
    }

    public EnergyMeter createEnergyMeter(EnergyMeter energyMeter) {
        return energyMeterRepository.save(energyMeter);
    }

    public void saveEnergyMeter(EnergyMeter energyMeter) {
        energyMeterRepository.save(energyMeter);
    }

    public List<EnergyMeter> getAllEnergyMeters() {
        return energyMeterRepository.findAll();
    }

    public Optional<EnergyMeter> getEnergyMeterById(Long energyMeterId) {
        return energyMeterRepository.findById(energyMeterId);
    }

    public EnergyMeter updateEnergyMeter(Long energyMeterId, EnergyMeter energyMeterDetails) {
        return energyMeterRepository.findById(energyMeterId)
                .map(energyMeter -> {
                    energyMeter.setMeterName(energyMeterDetails.getMeterName());
                    return energyMeterRepository.save(energyMeter);
                })
                .orElseThrow(() -> new RuntimeException("EnergyMeter not found"));
    }

    public void deleteEnergyMeter(Long energyMeterId) {
        energyMeterRepository.deleteById(energyMeterId);
    }
}
