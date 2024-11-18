package br.com.fiap.energyapi.domain.deviceAnalysis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device-analysis")
public class DeviceAnalysisController {

    private final DeviceAnalysisService deviceAnalysisService;

    @Autowired
    public DeviceAnalysisController(DeviceAnalysisService deviceAnalysisService) {
        this.deviceAnalysisService = deviceAnalysisService;
    }

    @PostMapping
    public ResponseEntity<DeviceAnalysis> createDeviceAnalysis(@RequestBody DeviceAnalysis deviceAnalysis) {
        DeviceAnalysis createdDeviceAnalysis = deviceAnalysisService.createDeviceAnalysis(deviceAnalysis);
        return ResponseEntity.ok(createdDeviceAnalysis);
    }

    @GetMapping
    public ResponseEntity<List<DeviceAnalysis>> getAllDeviceAnalyses() {
        List<DeviceAnalysis> analyses = deviceAnalysisService.getAllDeviceAnalyses();
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/{deviceAnalysisId}")
    public ResponseEntity<DeviceAnalysis> getDeviceAnalysisById(@PathVariable Long deviceAnalysisId) {
        return deviceAnalysisService.getDeviceAnalysisById(deviceAnalysisId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{deviceAnalysisId}")
    public ResponseEntity<DeviceAnalysis> updateDeviceAnalysis(@PathVariable Long deviceAnalysisId, @RequestBody DeviceAnalysis deviceAnalysisDetails) {
        try {
            DeviceAnalysis updatedAnalysis = deviceAnalysisService.updateDeviceAnalysis(deviceAnalysisId, deviceAnalysisDetails);
            return ResponseEntity.ok(updatedAnalysis);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{deviceAnalysisId}")
    public ResponseEntity<Void> deleteDeviceAnalysis(@PathVariable Long deviceAnalysisId) {
        deviceAnalysisService.deleteDeviceAnalysis(deviceAnalysisId);
        return ResponseEntity.noContent().build();
    }
}
