package br.com.fiap.energyapi.views;

import br.com.fiap.energyapi.domain.device.Device;
import br.com.fiap.energyapi.domain.device.DeviceRepository;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysis;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysisService;
import br.com.fiap.energyapi.domain.user.User;
import br.com.fiap.energyapi.domain.user.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("vaadin-device-analysis")
public class DeviceAnalysisView extends VerticalLayout {

    private final DeviceAnalysisService deviceAnalysisService;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final Grid<DeviceAnalysis> analysisGrid = new Grid<>(DeviceAnalysis.class);

    private final TextField currentWattsField = new TextField("Potência Atual (Watts)");
    private final TextField energyUsageMonthlyField = new TextField("Uso de Energia Mensal (kWh)");
    private final TextField efficiencyClassField = new TextField("Classe de Eficiência");
    private final TextField deviceIdField = new TextField("ID do dispositivo");
    private final TextField userIdField = new TextField("ID do Usuário");

    private DeviceAnalysis selectedAnalysis;

    @Autowired
    public DeviceAnalysisView(DeviceAnalysisService deviceAnalysisService, UserRepository userRepository, DeviceRepository deviceRepository) {
        this.deviceAnalysisService = deviceAnalysisService;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;

        add(new com.vaadin.flow.component.html.H1("Gerenciamento de Análises de Dispositivos"));

        analysisGrid.setColumns("deviceAnalysisId", "deviceCurrentWatts", "energyUsageMonthly", "efficiencyClass", "device.deviceId", "user.userId");
        analysisGrid.setItems(deviceAnalysisService.getAllDeviceAnalyses());
        analysisGrid.asSingleSelect().addValueChangeListener(event -> selectDeviceAnalysis(event.getValue()));

        Button saveButton = new Button("Salvar Análise");
        saveButton.addClickListener(e -> saveDeviceAnalysis());

        Button deleteButton = new Button("Excluir Análise");
        deleteButton.addClickListener(e -> deleteDeviceAnalysis());

        add(analysisGrid, currentWattsField, energyUsageMonthlyField, efficiencyClassField, deviceIdField, userIdField, saveButton, deleteButton);
    }

    private void selectDeviceAnalysis(DeviceAnalysis analysis) {
        if (analysis != null) {
            selectedAnalysis = analysis;
            currentWattsField.setValue(String.valueOf(analysis.getDeviceCurrentWatts()));
            energyUsageMonthlyField.setValue(String.valueOf(analysis.getEnergyUsageMonthly()));
            efficiencyClassField.setValue(analysis.getEfficiencyClass());
            deviceIdField.setValue(analysis.getDevice() != null ? String.valueOf(analysis.getDevice().getDeviceId()) : "");
            userIdField.setValue(analysis.getUser() != null ? String.valueOf(analysis.getUser().getUserId()) : "");
        } else {
            clearForm();
        }
    }

    private void saveDeviceAnalysis() {
        if (deviceIdField.isEmpty() || userIdField.isEmpty()) {
            Notification.show("Por favor, preencha todos os campos obrigatórios.");
            return;
        }

        if (selectedAnalysis == null) {
            selectedAnalysis = new DeviceAnalysis();
        }
        selectedAnalysis.setDeviceCurrentWatts(parseIntegerField(currentWattsField.getValue()));
        selectedAnalysis.setEnergyUsageMonthly(parseDoubleField(energyUsageMonthlyField.getValue()));
        selectedAnalysis.setEfficiencyClass(efficiencyClassField.getValue());

        try {
            Long deviceId = Long.valueOf(deviceIdField.getValue());
            Device device = deviceRepository.findById(deviceId).orElseThrow(() ->
                    new RuntimeException("Usuário não encontrado com o ID: " + deviceId));
            selectedAnalysis.setDevice(device);

            String userId = userIdField.getValue();
            User user = userRepository.findById(userId).orElseThrow(() ->
                    new RuntimeException("Usuário não encontrado com o ID: " + userId));
            selectedAnalysis.setUser(user);

            deviceAnalysisService.saveDeviceAnalysis(selectedAnalysis);
            analysisGrid.setItems(deviceAnalysisService.getAllDeviceAnalyses());
            clearForm();
            Notification.show("Análise de dispositivo salva com sucesso!");
        } catch (NumberFormatException e) {
            Notification.show("ID do Usuário inválido. Por favor, insira um valor numérico.");
        } catch (RuntimeException e) {
            Notification.show(e.getMessage());
        }
    }

    private void deleteDeviceAnalysis() {
        if (selectedAnalysis != null) {
            deviceAnalysisService.deleteDeviceAnalysis(selectedAnalysis.getDeviceAnalysisId());
            analysisGrid.setItems(deviceAnalysisService.getAllDeviceAnalyses());
            clearForm();
            Notification.show("Análise de dispositivo excluída com sucesso!");
        } else {
            Notification.show("Selecione uma análise para excluir.");
        }
    }

    private void clearForm() {
        selectedAnalysis = null;
        currentWattsField.clear();
        energyUsageMonthlyField.clear();
        efficiencyClassField.clear();
        deviceIdField.clear();
        userIdField.clear();
    }

    private int parseIntegerField(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDoubleField(String value    ) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
