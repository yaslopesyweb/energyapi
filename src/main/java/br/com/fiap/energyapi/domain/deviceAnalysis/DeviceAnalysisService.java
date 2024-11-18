package br.com.fiap.energyapi.domain.deviceAnalysis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceAnalysisService {

    private final DeviceAnalysisRepository deviceAnalysisRepository;

    @Autowired
    public DeviceAnalysisService(DeviceAnalysisRepository deviceAnalysisRepository) {
        this.deviceAnalysisRepository = deviceAnalysisRepository;
    }

    public DeviceAnalysis createDeviceAnalysis(DeviceAnalysis deviceAnalysis) {
        return deviceAnalysisRepository.save(deviceAnalysis);
    }

    public void saveDeviceAnalysis(DeviceAnalysis deviceAnalysis) {
        deviceAnalysisRepository.save(deviceAnalysis);
    }

    public List<DeviceAnalysis> getAllDeviceAnalyses() {
        return deviceAnalysisRepository.findAll();
    }

    public Optional<DeviceAnalysis> getDeviceAnalysisById(Long deviceAnalysisId) {
        return deviceAnalysisRepository.findById(deviceAnalysisId);
    }

    public DeviceAnalysis updateDeviceAnalysis(Long deviceAnalysisId, DeviceAnalysis deviceAnalysisDetails) {
        return deviceAnalysisRepository.findById(deviceAnalysisId)
                .map(deviceAnalysis -> {
                    deviceAnalysis.setDeviceCurrentWatts(deviceAnalysisDetails.getDeviceCurrentWatts());
                    deviceAnalysis.setEnergyUsageMonthly(deviceAnalysisDetails.getEnergyUsageMonthly());
                    deviceAnalysis.setEfficiencyClass(deviceAnalysisDetails.getEfficiencyClass());
                    return deviceAnalysisRepository.save(deviceAnalysis);
                })
                .orElseThrow(() -> new RuntimeException("DeviceAnalysis not found"));
    }

    public void deleteDeviceAnalysis(Long deviceAnalysisId) {
        deviceAnalysisRepository.deleteById(deviceAnalysisId);
    }
}
