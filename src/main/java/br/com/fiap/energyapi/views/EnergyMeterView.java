package br.com.fiap.energyapi.views;

import br.com.fiap.energyapi.domain.device.Device;
import br.com.fiap.energyapi.domain.device.DeviceRepository;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysis;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysisRepository;
import br.com.fiap.energyapi.domain.energyMeter.EnergyMeter;
import br.com.fiap.energyapi.domain.energyMeter.EnergyMeterService;
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

@Route("vaadin-energy-meters")
public class EnergyMeterView extends VerticalLayout {

    private final EnergyMeterService energyMeterService;
    private final DeviceRepository deviceRepository;
    private final DeviceAnalysisRepository deviceAnalysisRepository;
    private final ReportRepository reportRepository;
    private final Grid<EnergyMeter> meterGrid = new Grid<>(EnergyMeter.class);

    private final TextField meterNameField = new TextField("Nome do Medidor");
    private final ComboBox<User> userComboBox = new ComboBox<>("Usuário");
    private EnergyMeter selectedMeter;

    @Autowired
    public EnergyMeterView(EnergyMeterService energyMeterService, UserRepository userRepository, DeviceRepository deviceRepository, DeviceAnalysisRepository deviceAnalysisRepository, ReportRepository reportRepository) {
        this.energyMeterService = energyMeterService;
        this.deviceRepository = deviceRepository;
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

        add(new com.vaadin.flow.component.html.H2("Cadastro de Medidores de Energia"));

        // Configuração do ComboBox para seleção de usuários
        List<User> users = userRepository.findAll(); // Carregar todos os usuários disponíveis
        userComboBox.setItems(users);
        userComboBox.setItemLabelGenerator(User::getEmail);
        userComboBox.setPlaceholder("Selecione um usuário");

        // Configuração do Grid
        meterGrid.setColumns("energyMeterId", "meterName", "user.userId");
        meterGrid.setItems(energyMeterService.getAllEnergyMeters());
        meterGrid.asSingleSelect().addValueChangeListener(event -> selectMeter(event.getValue()));

        // Botões de ação com estilização
        Button saveButton = new Button("Salvar");
        applyButtonStyles(saveButton);
        Button deleteButton = new Button("Excluir");
        deleteButton.getStyle().set("background-color", "white").set("color", "red");

        saveButton.addClickListener(e -> saveMeter());
        deleteButton.addClickListener(e -> deleteMeter());

        // Ajuste dos campos com estilização
        applyTextFieldStyles(meterNameField);
        applyComboBoxStyles(userComboBox);

        // Centralizando Botões e Componentes
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Adicionando componentes à interface
        add(meterNameField, userComboBox, saveButton, deleteButton, meterGrid);
    }

    private void selectMeter(EnergyMeter meter) {
        if (meter != null) {
            selectedMeter = meter;
            meterNameField.setValue(meter.getMeterName());
            userComboBox.setValue(meter.getUser());
        } else {
            clearForm();
        }
    }

    private void saveMeter() {
        if (meterNameField.isEmpty() || userComboBox.isEmpty()) {
            Notification.show("Por favor, preencha todos os campos.", 1000, Notification.Position.TOP_START);
            return;
        }

        if (selectedMeter == null) {
            selectedMeter = new EnergyMeter();
        }
        selectedMeter.setMeterName(meterNameField.getValue());

        try {
            User user = userComboBox.getValue();
            selectedMeter.setUser(user);

            energyMeterService.saveEnergyMeter(selectedMeter);
            meterGrid.setItems(energyMeterService.getAllEnergyMeters());
            clearForm();
            Notification.show("Medidor salvo com sucesso!", 1000, Notification.Position.TOP_START);
        } catch (RuntimeException e) {
            Notification.show(e.getMessage(), 1000, Notification.Position.TOP_START);
        }
    }

    private void deleteMeter() {
        if (selectedMeter != null) {
            // Criar um diálogo de confirmação
            Dialog confirmationDialog = new Dialog();
            confirmationDialog.setCloseOnEsc(false);

            // Configuração do diálogo de confirmação
            Button confirmButton = new Button("Confirmar", event -> {
                try {
                    // Obter todos os dispositivos relacionados ao medidor
                    List<Device> relatedDevices = deviceRepository.findAll().stream()
                            .filter(device -> device.getEnergyMeter() != null && device.getEnergyMeter().getEnergyMeterId().equals(selectedMeter.getEnergyMeterId()))
                            .collect(Collectors.toList());

                    // Para cada dispositivo relacionado, excluir análises e relatórios relacionados
                    relatedDevices.forEach(device -> {
                        List<DeviceAnalysis> relatedAnalyses = deviceAnalysisRepository.findAll().stream()
                                .filter(analysis -> analysis.getDevice() != null && analysis.getDevice().getDeviceId().equals(device.getDeviceId()))
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
                    });

                    // Excluir dispositivos relacionados
                    deviceRepository.deleteAll(relatedDevices);

                    // Remova o medidor de energia
                    energyMeterService.deleteEnergyMeter(selectedMeter.getEnergyMeterId());
                    meterGrid.setItems(energyMeterService.getAllEnergyMeters());
                    clearForm();
                    Notification.show("Medidor e elementos relacionados excluídos com sucesso!", 1000, Notification.Position.TOP_START);
                } catch (RuntimeException e) {
                    Notification.show("Erro ao excluir o medidor: " + e.getMessage(), 1000, Notification.Position.TOP_START);
                }
                confirmationDialog.close();
            });

            Button cancelButton = new Button("Cancelar", event -> confirmationDialog.close());

            // Adiciona componentes ao diálogo de confirmação
            confirmationDialog.add(
                    new VerticalLayout(
                            new NativeLabel("Tem certeza de que deseja excluir este medidor? Esta ação removerá dispositivos, análises e relatórios relacionados."),
                            new HorizontalLayout(confirmButton, cancelButton)
                    )
            );

            confirmationDialog.open();
        } else {
            Notification.show("Selecione um medidor para excluir.", 1000, Notification.Position.TOP_START);
        }
    }




    private void clearForm() {
        selectedMeter = null;
        meterNameField.clear();
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
}
