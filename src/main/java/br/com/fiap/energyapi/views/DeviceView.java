package br.com.fiap.energyapi.views;

import br.com.fiap.energyapi.domain.device.Device;
import br.com.fiap.energyapi.domain.device.DeviceService;
import br.com.fiap.energyapi.domain.energyMeter.EnergyMeter;
import br.com.fiap.energyapi.domain.energyMeter.EnergyMeterRepository;
import br.com.fiap.energyapi.domain.user.User;
import br.com.fiap.energyapi.domain.user.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("vaadin-devices")
public class DeviceView extends VerticalLayout {

    private final DeviceService deviceService;
    private final UserRepository userRepository;
    private final EnergyMeterRepository energyMeterRepository;
    private final Grid<Device> deviceGrid = new Grid<>(Device.class);

    private final TextField deviceNameField = new TextField("Nome do Dispositivo");
    private final TextField deviceTypeField = new TextField("Tipo do Dispositivo");
    private final TextField energyMeterIdField = new TextField("ID do Medidor de Energia");
    private final TextField usageHoursField = new TextField("Horas Estimadas de Uso");
    private final TextField userIdField = new TextField("ID do Usuário");
    private Device selectedDevice;

    @Autowired
    public DeviceView(DeviceService deviceService, UserRepository userRepository, EnergyMeterRepository energyMeterRepository) {
        this.deviceService = deviceService;
        this.userRepository = userRepository;
        this.energyMeterRepository = energyMeterRepository;

        add(new com.vaadin.flow.component.html.H1("Gerenciamento de Dispositivos"));

        deviceGrid.setColumns("deviceId", "deviceName", "deviceType", "energyMeter.energyMeterId", "estimatedUsageHours", "user.userId");
        deviceGrid.setItems(deviceService.getAllDevices());
        deviceGrid.asSingleSelect().addValueChangeListener(event -> selectDevice(event.getValue()));

        Button saveButton = new Button("Salvar Dispositivo");
        saveButton.addClickListener(e -> saveDevice());

        Button deleteButton = new Button("Excluir Dispositivo");
        deleteButton.addClickListener(e -> deleteDevice());

        add(deviceGrid, deviceNameField, deviceTypeField, energyMeterIdField, usageHoursField, userIdField, saveButton, deleteButton);
    }

    private void selectDevice(Device device) {
        if (device != null) {
            selectedDevice = device;
            deviceNameField.setValue(device.getDeviceName());
            deviceTypeField.setValue(device.getDeviceType());
            usageHoursField.setValue(device.getEstimatedUsageHours());
            energyMeterIdField.setValue(device.getEnergyMeter() != null ? String.valueOf(device.getUser().getUserId()) : "");
            userIdField.setValue(device.getUser() != null ? String.valueOf(device.getUser().getUserId()) : "");
        } else {
            clearForm();
        }
    }

    private void saveDevice() {
        if (deviceNameField.isEmpty() || deviceTypeField.isEmpty() || energyMeterIdField.isEmpty() || usageHoursField.isEmpty() || userIdField.isEmpty()) {
            Notification.show("Por favor, preencha todos os campos.");
            return;
        }

        if (selectedDevice == null) {
            selectedDevice = new Device();
        }
        selectedDevice.setDeviceName(deviceNameField.getValue());
        selectedDevice.setDeviceType(deviceTypeField.getValue());
        selectedDevice.setEstimatedUsageHours(usageHoursField.getValue());

        try {
            Long energyMeterId = Long.valueOf(energyMeterIdField.getValue());
            EnergyMeter energyMeter = energyMeterRepository.findById(energyMeterId).orElseThrow(() ->
                    new RuntimeException("Usuário não encontrado com o ID: " + energyMeterId));
            selectedDevice.setEnergyMeter(energyMeter);

            String userId = userIdField.getValue();
            User user = userRepository.findById(userId).orElseThrow(() ->
                    new RuntimeException("Usuário não encontrado com o ID: " + userId));
            selectedDevice.setUser(user);

            deviceService.saveDevice(selectedDevice);
            deviceGrid.setItems(deviceService.getAllDevices());
            clearForm();
            Notification.show("Dispositivo salvo com sucesso!");
        } catch (NumberFormatException e) {
            Notification.show("ID do Usuário inválido. Por favor, insira um valor numérico.");
        } catch (RuntimeException e) {
            Notification.show(e.getMessage());
        }
    }


    private void deleteDevice() {
        if (selectedDevice != null) {
            deviceService.deleteDevice(selectedDevice.getDeviceId());
            deviceGrid.setItems(deviceService.getAllDevices());
            clearForm();
            Notification.show("Dispositivo excluído com sucesso!");
        } else {
            Notification.show("Selecione um dispositivo para excluir.");
        }
    }

    private void clearForm() {
        selectedDevice = null;
        deviceNameField.clear();
        deviceTypeField.clear();
        energyMeterIdField.clear();
        usageHoursField.clear();
        userIdField.clear();
    }
}
