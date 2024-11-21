package br.com.fiap.energyapi.views;

import br.com.fiap.energyapi.domain.device.Device;
import br.com.fiap.energyapi.domain.device.DeviceService;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysis;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysisRepository;
import br.com.fiap.energyapi.domain.energyMeter.EnergyMeter;
import br.com.fiap.energyapi.domain.energyMeter.EnergyMeterRepository;
import br.com.fiap.energyapi.domain.report.Report;
import br.com.fiap.energyapi.domain.report.ReportRepository;
import br.com.fiap.energyapi.domain.user.User;
import br.com.fiap.energyapi.domain.user.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.dialog.Dialog;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Route("vaadin-devices")
public class DeviceView extends VerticalLayout {

    private final DeviceService deviceService;
    private final DeviceAnalysisRepository deviceAnalysisRepository;
    private final ReportRepository reportRepository;
    private final Grid<Device> deviceGrid = new Grid<>(Device.class);

    private final TextField deviceNameField = new TextField("Nome do Dispositivo");
    private final ComboBox<String> deviceTypeComboBox = new ComboBox<>("Tipo do Dispositivo");
    private final TextField usageHoursField = new TextField("Tempo de uso diário");
    private final ComboBox<String> usageUnitComboBox = new ComboBox<>();
    private final ComboBox<EnergyMeter> energyMeterComboBox = new ComboBox<>("Medidor de Energia");
    private final ComboBox<User> userComboBox = new ComboBox<>("Usuário");
    private Device selectedDevice;

    @Autowired
    public DeviceView(DeviceService deviceService, UserRepository userRepository, EnergyMeterRepository energyMeterRepository, DeviceAnalysisRepository deviceAnalysisRepository, ReportRepository reportRepository) {
        this.deviceService = deviceService;
        this.deviceAnalysisRepository = deviceAnalysisRepository;
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

        add(new com.vaadin.flow.component.html.H2("Gerenciamento de Dispositivos"));

        // Configuração do ComboBox para tipo de dispositivo
        deviceTypeComboBox.setItems(
                "Ar Condicionado",
                "Fogão",
                "Micro-ondas",
                "Forno elétrico",
                "Lâmpada",
                "Lavador de roupa",
                "Refrigerador",
                "Televisor",
                "Ventilador"
        );
        deviceTypeComboBox.setPlaceholder("Selecione o tipo de dispositivo");

        // Configuração do ComboBox para seleção de unidade de tempo
        usageUnitComboBox.setItems("Horas", "Minutos");
        usageUnitComboBox.setPlaceholder("Selecione a unidade");
        usageUnitComboBox.setValue("Horas"); // Valor padrão

        // Configuração do ComboBox para seleção de Medidores de Energia
        List<EnergyMeter> energyMeters = energyMeterRepository.findAll(); // Carregar todos os medidores de energia inicialmente
        energyMeterComboBox.setItems(energyMeters);
        energyMeterComboBox.setItemLabelGenerator(em -> "- " + em.getMeterName());
        energyMeterComboBox.setPlaceholder("Selecione um medidor de energia");

        // Configuração do ComboBox para seleção de Usuários
        List<User> users = userRepository.findAll(); // Carregar todos os usuários
        userComboBox.setItems(users);
        userComboBox.setItemLabelGenerator(User::getEmail);
        userComboBox.setPlaceholder("Selecione um usuário");

        // Adicionar listener para filtrar medidores com base no usuário selecionado e verificar se o medidor está em uso
        userComboBox.addValueChangeListener(event -> {
            if (!isGridSelection) { // Somente aplica a lógica de filtragem quando a seleção não é feita pela grid
                User selectedUser = event.getValue();
                if (selectedUser != null) {
                    List<EnergyMeter> filteredMeters = energyMeterRepository.findAll().stream()
                            .filter(meter -> meter.getUser() != null && meter.getUser().getUserId().equals(selectedUser.getUserId()))
                            .filter(meter -> deviceService.getAllDevices().stream()
                                    .noneMatch(device -> device.getEnergyMeter() != null && device.getEnergyMeter().getEnergyMeterId().equals(meter.getEnergyMeterId())))
                            .collect(Collectors.toList());

                    if (filteredMeters.isEmpty()) {
                        Notification.show("Não há medidores disponíveis para uso.", 3500, Notification.Position.TOP_START);
                        energyMeterComboBox.setEnabled(false); // Bloqueia a seleção caso não haja medidores disponíveis
                    } else {
                        energyMeterComboBox.setEnabled(true); // Habilita a seleção caso existam medidores disponíveis
                    }

                    energyMeterComboBox.setItems(filteredMeters);
                    energyMeterComboBox.clear();
                } else {
                    energyMeterComboBox.setItems();
                    energyMeterComboBox.setEnabled(false); // Bloqueia a seleção se nenhum usuário for selecionado
                    energyMeterComboBox.clear();
                }
            }
        });

        // Desabilitar o ComboBox de medidores inicialmente
        energyMeterComboBox.setEnabled(false);




        // Configuração do Grid
        deviceGrid.setColumns("deviceId", "deviceName", "deviceType", "energyMeter.energyMeterId", "estimatedUsageHours", "user.userId");
        deviceGrid.setItems(deviceService.getAllDevices());
        deviceGrid.asSingleSelect().addValueChangeListener(event -> selectDevice(event.getValue()));

        // Botões de ação com estilização
        Button saveButton = new Button("Salvar");
        applyButtonStyles(saveButton);
        Button deleteButton = new Button("Excluir");
        deleteButton.getStyle().set("background-color", "white").set("color", "red");

        saveButton.addClickListener(e -> saveDevice());
        deleteButton.addClickListener(e -> deleteDevice());

        // Ajuste dos campos com estilização
        applyTextFieldStyles(deviceNameField);
        applyComboBoxStyles(deviceTypeComboBox);
        applyTextFieldComboStyles(usageHoursField);
        applyComboBoxTextStyles(usageUnitComboBox);
        applyComboBoxStyles(energyMeterComboBox);
        applyComboBoxStyles(userComboBox);

        // Layout para o campo de horas estimadas de uso e unidade
        HorizontalLayout usageLayout = new HorizontalLayout(usageHoursField, usageUnitComboBox);
        usageLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);

        // Centralizando Botões e Componentes
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Adicionando componentes à interface
        add(userComboBox, deviceNameField, deviceTypeComboBox, energyMeterComboBox, usageLayout, saveButton, deleteButton, deviceGrid);
    }

    private boolean isGridSelection = false;


    private void selectDevice(Device device) {
        isGridSelection = true; // Define que a seleção está sendo feita pela grid
        if (device != null) {
            selectedDevice = device;
            deviceNameField.setValue(device.getDeviceName());
            deviceTypeComboBox.setValue(device.getDeviceType());
            usageHoursField.setValue(device.getEstimatedUsageHours());

            // Desbloquear o ComboBox de medidores e definir o medidor associado ao dispositivo
            energyMeterComboBox.setEnabled(true);
            energyMeterComboBox.setValue(device.getEnergyMeter());

            userComboBox.setValue(device.getUser());
        } else {
            clearForm();
        }
        isGridSelection = false; // Restaura a flag após a seleção
    }


    private void saveDevice() {
        if (deviceNameField.isEmpty() || deviceTypeComboBox.isEmpty() || energyMeterComboBox.isEmpty() || usageHoursField.isEmpty() || userComboBox.isEmpty()) {
            Notification.show("Por favor, preencha todos os campos.", 1000, Notification.Position.TOP_START);
            return;
        }

        if (selectedDevice == null) {
            selectedDevice = new Device();
        }
        selectedDevice.setDeviceName(deviceNameField.getValue());
        selectedDevice.setDeviceType(deviceTypeComboBox.getValue());

        double estimatedUsage = Double.parseDouble(usageHoursField.getValue());
        if ("Minutos".equals(usageUnitComboBox.getValue())) {
            estimatedUsage /= 60; // Converter minutos para horas
        }
        selectedDevice.setEstimatedUsageHours(String.valueOf(estimatedUsage));

        try {
            EnergyMeter energyMeter = energyMeterComboBox.getValue();
            selectedDevice.setEnergyMeter(energyMeter);

            User user = userComboBox.getValue();
            selectedDevice.setUser(user);

            deviceService.saveDevice(selectedDevice);
            deviceGrid.setItems(deviceService.getAllDevices());
            clearForm();
            Notification.show("Dispositivo salvo com sucesso!", 1000, Notification.Position.TOP_START);
        } catch (RuntimeException e) {
            Notification.show(e.getMessage(), 1000, Notification.Position.TOP_START);
        }
    }

    private void deleteDevice() {
        if (selectedDevice != null) {
            // Criar um diálogo de confirmação
            Dialog confirmationDialog = new Dialog();
            confirmationDialog.setCloseOnEsc(false);

            // Configuração do diálogo de confirmação
            Button confirmButton = new Button("Confirmar", event -> {
                try {
                    // Obtenha todas as análises relacionadas ao dispositivo
                    List<DeviceAnalysis> relatedAnalyses = deviceAnalysisRepository.findAll().stream()
                            .filter(analysis -> analysis.getDevice() != null && analysis.getDevice().getDeviceId().equals(selectedDevice.getDeviceId()))
                            .collect(Collectors.toList());

                    // Excluir relatórios relacionados às análises
                    relatedAnalyses.forEach(analysis -> {
                        List<Report> relatedReports = reportRepository.findAll().stream()
                                .filter(report -> report.getDeviceAnalysis() != null && report.getDeviceAnalysis().getDeviceAnalysisId().equals(analysis.getDeviceAnalysisId()))
                                .collect(Collectors.toList());
                        reportRepository.deleteAll(relatedReports);
                    });

                    // Excluir análises relacionadas
                    deviceAnalysisRepository.deleteAll(relatedAnalyses);

                    // Remova o dispositivo
                    deviceService.deleteDevice(selectedDevice.getDeviceId());
                    deviceGrid.setItems(deviceService.getAllDevices());
                    clearForm();
                    Notification.show("Dispositivo excluído com sucesso!", 1000, Notification.Position.TOP_START);
                } catch (RuntimeException e) {
                    Notification.show("Erro ao excluir o dispositivo: " + e.getMessage(), 1000, Notification.Position.TOP_START);
                }
                confirmationDialog.close();
            });

            Button cancelButton = new Button("Cancelar", event -> confirmationDialog.close());

            // Adiciona componentes ao diálogo de confirmação
            confirmationDialog.add(
                    new VerticalLayout(
                            new NativeLabel("Tem certeza de que deseja excluir este dispositivo? Esta ação removerá análises e relatórios relacionados."),
                            new HorizontalLayout(confirmButton, cancelButton)
                    )
            );

            confirmationDialog.open();
        } else {
            Notification.show("Selecione um dispositivo para excluir.", 1000, Notification.Position.TOP_START);
        }
    }



    private void clearForm() {
        selectedDevice = null;
        deviceNameField.clear();
        deviceTypeComboBox.clear();
        energyMeterComboBox.clear();
        usageHoursField.clear();
        usageUnitComboBox.setValue("Horas");
        userComboBox.clear();
    }

    private void applyButtonStyles(Button button) {
        String buttonColor = "#1E3E69";
        button.getStyle().set("background-color", buttonColor).set("color", "white");
        button.setWidth("377px");
        button.setHeight("55px");
        button.getStyle().set("margin", "3px");
    }

    private void applyTextFieldStyles(TextField textField) {
        textField.setWidth("377px");
        textField.getStyle().set("margin", "3px");
    }

    private void applyComboBoxStyles(ComboBox<?> comboBox) {
        comboBox.setWidth("377px");
        comboBox.getStyle().set("margin", "3px");
    }

    private void applyTextFieldComboStyles(TextField textField) {
        textField.setWidth("170x");
        textField.getStyle().set("marginTop", "3px");
        textField.getStyle().set("marginBottom", "3px");
    }

    private void applyComboBoxTextStyles(ComboBox<?> comboBox) {
        comboBox.setWidth("170px");
        comboBox.getStyle().set("marginTop", "3px");
        comboBox.getStyle().set("marginBottom", "3px");
    }
}
