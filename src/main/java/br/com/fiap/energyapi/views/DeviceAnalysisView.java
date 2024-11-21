package br.com.fiap.energyapi.views;

import br.com.fiap.energyapi.domain.device.Device;
import br.com.fiap.energyapi.domain.device.DeviceRepository;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysis;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysisService;
import br.com.fiap.energyapi.domain.report.Report;
import br.com.fiap.energyapi.domain.report.ReportRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.dialog.Dialog;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Route("vaadin-device-analysis")
public class DeviceAnalysisView extends VerticalLayout {

    private final DeviceAnalysisService deviceAnalysisService;
    private final ReportRepository reportRepository;
    private final Grid<DeviceAnalysis> analysisGrid = new Grid<>(DeviceAnalysis.class);
    private final ComboBox<Device> deviceComboBox = new ComboBox<>("Dispositivo");

    private DeviceAnalysis selectedAnalysis;

    @Autowired
    public DeviceAnalysisView(DeviceAnalysisService deviceAnalysisService, DeviceRepository deviceRepository, ReportRepository reportRepository) {
        this.deviceAnalysisService = deviceAnalysisService;
        this.reportRepository = reportRepository;

        // Adicionando a logo
        Image logo = new Image("images/logo-text.png", "Logo do Projeto");
        logo.setHeight("40px");
        add(logo);

        // Botão para voltar ao menu principal com estilização
        Button backButton = new Button("Voltar ao Menu", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.getStyle().set("background-color", "white").set("color", "#1E3E69");
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("vaadin"))
        );
        add(backButton);

        add(new com.vaadin.flow.component.html.H2("Gerenciamento de Análises de Dispositivos"));

        // Configuração do ComboBox para seleção de dispositivos
        List<Device> devices = deviceRepository.findAll(); // Carregar todos os dispositivos
        deviceComboBox.setItems(devices);
        deviceComboBox.setItemLabelGenerator(d -> "ID: " + d.getDeviceId() + " - " + d.getDeviceName());
        deviceComboBox.setPlaceholder("Selecione um dispositivo");

        analysisGrid.setColumns("deviceAnalysisId", "deviceCurrentWatts", "energyUsageMonthly", "efficiencyClass", "device.deviceId", "user.userId");
        analysisGrid.setItems(deviceAnalysisService.getAllDeviceAnalyses());
        analysisGrid.asSingleSelect().addValueChangeListener(event -> selectDeviceAnalysis(event.getValue()));

        // Botões de ação com estilização
        Button saveButton = new Button("Salvar");
        applyButtonStyles(saveButton);
        Button deleteButton = new Button("Excluir");
        deleteButton.getStyle().set("background-color", "white").set("color", "red");

        // Ajuste do ComboBox de dispositivo com estilização
        applyComboBoxStyles(deviceComboBox);

        saveButton.addClickListener(e -> saveDeviceAnalysis());
        deleteButton.addClickListener(e -> deleteDeviceAnalysis());

        // Centralizando Botões e Componentes
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Adicionando componentes à interface
        add(deviceComboBox, saveButton, deleteButton, analysisGrid);
    }

    private void selectDeviceAnalysis(DeviceAnalysis analysis) {
        if (analysis != null) {
            selectedAnalysis = analysis;
            deviceComboBox.setValue(analysis.getDevice());
        } else {
            clearForm();
        }
    }

    private void saveDeviceAnalysis() {
        if (deviceComboBox.isEmpty()) {
            Notification.show("Por favor, selecione um dispositivo.", 1000, Notification.Position.TOP_START);
            return;
        }

        if (selectedAnalysis == null) {
            selectedAnalysis = new DeviceAnalysis();
        }

        Device selectedDevice = deviceComboBox.getValue();
        double dailyUsageHours = Double.parseDouble(selectedDevice.getEstimatedUsageHours()); // Obter horas de uso diário do dispositivo
        double generatedPower = generateRandomPower(selectedDevice.getDeviceType());
        double monthlyUsage = calculateMonthlyUsage(generatedPower, dailyUsageHours);
        String efficiencyClass = classifyEfficiency(selectedDevice.getDeviceType(), monthlyUsage);

        selectedAnalysis.setDeviceCurrentWatts((int) generatedPower);
        selectedAnalysis.setEnergyUsageMonthly(monthlyUsage);
        selectedAnalysis.setEfficiencyClass(efficiencyClass);
        selectedAnalysis.setDevice(selectedDevice);
        selectedAnalysis.setUser(selectedDevice.getUser()); // Injeta automaticamente o usuário associado ao dispositivo

        try {
            deviceAnalysisService.saveDeviceAnalysis(selectedAnalysis);
            analysisGrid.setItems(deviceAnalysisService.getAllDeviceAnalyses());
            clearForm();
            Notification.show("Análise de dispositivo salva com sucesso!", 1000, Notification.Position.TOP_START);
        } catch (RuntimeException e) {
            Notification.show(e.getMessage(), 1000, Notification.Position.MIDDLE);
        }
    }

    private double generateRandomPower(String deviceType) {
        Random random = new Random();
        return switch (deviceType) {
            case "Ar Condicionado" -> 900 + random.nextInt(1100); // 900W a 2000W
            case "Fogão" -> 1000 + random.nextInt(500); // 1000W a 1500W
            case "Micro-ondas" -> 600 + random.nextInt(400); // 600W a 1000W
            case "Forno elétrico" -> 1000 + random.nextInt(1000); // 1000W a 2000W
            case "Lâmpada" -> 10 + random.nextInt(90); // 10W a 100W
            case "Lavador de roupa" -> 500 + random.nextInt(1000); // 500W a 1500W
            case "Refrigerador" -> 100 + random.nextInt(400); // 100W a 500W
            case "Televisor" -> 50 + random.nextInt(150); // 50W a 200W
            case "Ventilador" -> 30 + random.nextInt(70); // 30W a 100W
            default -> 100 + random.nextInt(400); // Padrão: 100W a 500W
        };
    }

    private double calculateMonthlyUsage(double power, double hoursPerDay) {
        return power * hoursPerDay * 30 / 1000; // Calcular kWh por mês
    }

    private String classifyEfficiency(String deviceType, double monthlyUsage) {
        switch (deviceType) {
            case "Ar Condicionado":
                if (monthlyUsage <= 100) return "A";
                if (monthlyUsage <= 200) return "B";
                if (monthlyUsage <= 300) return "C";
                return "D";
            case "Fogão":
                if (monthlyUsage <= 20) return "A";
                if (monthlyUsage <= 40) return "B";
                if (monthlyUsage <= 60) return "C";
                return "D";
            case "Micro-ondas", "Televisor":
                if (monthlyUsage <= 30) return "A";
                if (monthlyUsage <= 60) return "B";
                if (monthlyUsage <= 90) return "C";
                return "D";
            case "Forno elétrico", "Refrigerador":
                if (monthlyUsage <= 50) return "A";
                if (monthlyUsage <= 100) return "B";
                if (monthlyUsage <= 150) return "C";
                return "D";
            case "Lâmpada":
                if (monthlyUsage <= 5) return "A";
                if (monthlyUsage <= 10) return "B";
                if (monthlyUsage <= 15) return "C";
                return "D";
            case "Lavador de roupa":
                if (monthlyUsage <= 80) return "A";
                if (monthlyUsage <= 150) return "B";
                if (monthlyUsage <= 200) return "C";
                return "D";
            case "Ventilador":
                if (monthlyUsage <= 15) return "A";
                if (monthlyUsage <= 30) return "B";
                if (monthlyUsage <= 50) return "C";
                return "D";
            default:
                if (monthlyUsage <= 50) return "A";
                if (monthlyUsage <= 100) return "B";
                if (monthlyUsage <= 200) return "C";
                return "D";
        }
    }

    private void deleteDeviceAnalysis() {
        if (selectedAnalysis != null) {
            // Criar um diálogo de confirmação
            Dialog confirmationDialog = new Dialog();
            confirmationDialog.setCloseOnEsc(false);

            // Configuração do diálogo de confirmação
            Button confirmButton = new Button("Confirmar", event -> {
                try {
                    // Remova todos os relatórios relacionados à análise selecionada
                    List<Report> relatedReports = reportRepository.findAll().stream()
                            .filter(report -> report.getDeviceAnalysis() != null && report.getDeviceAnalysis().getDeviceAnalysisId().equals(selectedAnalysis.getDeviceAnalysisId()))
                            .collect(Collectors.toList());
                    reportRepository.deleteAll(relatedReports);

                    // Remova a análise selecionada
                    deviceAnalysisService.deleteDeviceAnalysis(selectedAnalysis.getDeviceAnalysisId());
                    analysisGrid.setItems(deviceAnalysisService.getAllDeviceAnalyses());
                    clearForm();
                    Notification.show("Análise de dispositivo excluída com sucesso!", 1000, Notification.Position.TOP_START);
                } catch (RuntimeException e) {
                    Notification.show("Erro ao excluir a análise: " + e.getMessage(), 1000, Notification.Position.TOP_START);
                }
                confirmationDialog.close();
            });

            Button cancelButton = new Button("Cancelar", event -> confirmationDialog.close());

            // Adiciona componentes ao diálogo de confirmação
            confirmationDialog.add(
                    new VerticalLayout(
                            new NativeLabel("Tem certeza de que deseja excluir esta análise? Esta ação removerá relatórios relacionados."),
                            new HorizontalLayout(confirmButton, cancelButton)
                    )
            );

            confirmationDialog.open();
        } else {
            Notification.show("Selecione uma análise para excluir.", 1000, Notification.Position.TOP_START);
        }
    }

    private void clearForm() {
        selectedAnalysis = null;
        deviceComboBox.clear();
    }

    private void applyButtonStyles(Button button) {
        String buttonColor = "#1E3E69";
        button.getStyle().set("background-color", buttonColor).set("color", "white");
        button.setWidth("377px");
        button.setHeight("55px");
        button.getStyle().set("margin", "3px");
    }

    private void applyComboBoxStyles(ComboBox<?> comboBox) {
        comboBox.setWidth("377px");
        comboBox.getStyle().set("margin", "3px");
    }
}

