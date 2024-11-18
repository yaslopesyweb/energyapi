package br.com.fiap.energyapi.domain.device;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Device createDevice(Device device) {
        return deviceRepository.save(device);
    }

    public void saveDevice(Device device) {
        deviceRepository.save(device);
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Optional<Device> getDeviceById(Long deviceId) {
        return deviceRepository.findById(deviceId);
    }

    public Device updateDevice(Long deviceId, Device deviceDetails) {
        return deviceRepository.findById(deviceId)
                .map(device -> {
                    device.setDeviceName(deviceDetails.getDeviceName());
                    device.setDeviceType(deviceDetails.getDeviceType());
                    device.setEstimatedUsageHours(deviceDetails.getEstimatedUsageHours());
                    return deviceRepository.save(device);
                })
                .orElseThrow(() -> new RuntimeException("Device not found"));
    }

    public void deleteDevice(Long deviceId) {
        deviceRepository.deleteById(deviceId);
    }

}
