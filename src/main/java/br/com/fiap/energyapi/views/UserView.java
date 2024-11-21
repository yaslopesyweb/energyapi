package br.com.fiap.energyapi.views;

import br.com.fiap.energyapi.domain.device.Device;
import br.com.fiap.energyapi.domain.device.DeviceRepository;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysis;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysisRepository;
import br.com.fiap.energyapi.domain.energyMeter.EnergyMeter;
import br.com.fiap.energyapi.domain.energyMeter.EnergyMeterRepository;
import br.com.fiap.energyapi.domain.report.Report;
import br.com.fiap.energyapi.domain.report.ReportRepository;
import br.com.fiap.energyapi.domain.user.User;
import br.com.fiap.energyapi.domain.user.UserService;
import com.vaadin.flow.component.button.Button;
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

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Route("vaadin-users")
public class UserView extends VerticalLayout {

    private final UserService userService;
    private final EnergyMeterRepository energyMeterRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceAnalysisRepository deviceAnalysisRepository;
    private final ReportRepository reportRepository;
    private final Grid<User> userGrid = new Grid<>(User.class); // Grid para listar usuários

    private final TextField emailField = new TextField("E-mail");

    private User selectedUser;

    @Autowired
    public UserView(UserService userService, EnergyMeterRepository energyMeterRepository, DeviceRepository deviceRepository, DeviceAnalysisRepository deviceAnalysisRepository, ReportRepository reportRepository) {
        this.userService = userService;
        this.energyMeterRepository = energyMeterRepository;
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

        add(new com.vaadin.flow.component.html.H2("Gerenciamento de Usuários"));

        // Configuração do Grid
        userGrid.setColumns("userId", "email", "createdAt");
        userGrid.setItems(userService.getAllUsers());
        userGrid.asSingleSelect().addValueChangeListener(event -> selectUser(event.getValue()));

        // Botões de ação com estilização
        Button saveButton = new Button("Salvar");
        applyButtonStyles(saveButton);
        Button deleteButton = new Button("Excluir");
        deleteButton.getStyle().set("background-color", "white").set("color", "red");

        saveButton.addClickListener(e -> saveUser());
        deleteButton.addClickListener(e -> deleteUser());

        // Ajuste do campo de texto com estilização
        applyTextFieldStyles(emailField);

        // Centralizando Botões
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Adicionando componentes à interface
        add( emailField, saveButton, deleteButton, userGrid);
    }

    private void selectUser(User user) {
        if (user != null) {
            selectedUser = user;
            emailField.setValue(user.getEmail());
        } else {
            clearForm();
        }
    }

    private void saveUser() {
        if (emailField.isEmpty()) {
            Notification.show("Por favor, preencha o campo de E-mail.", 1000, Notification.Position.MIDDLE);
            return;
        }

        if (selectedUser == null) {
            selectedUser = new User();
            selectedUser.setCreatedAt(Instant.now().toString()); // Data de criação automática no formato solicitado
        }
        selectedUser.setUserId(java.util.UUID.randomUUID().toString());
        selectedUser.setEmail(emailField.getValue());

        try {
            userService.saveUser(selectedUser);
            userGrid.setItems(userService.getAllUsers());
            clearForm();
            Notification.show("Usuário salvo com sucesso!", 1000, Notification.Position.TOP_START);
        } catch (RuntimeException e) {
            Notification.show("Erro ao salvar usuário: " + e.getMessage(), 1000, Notification.Position.TOP_START);
        }
    }

    private void deleteUser() {
        if (selectedUser != null) {
            // Criar um diálogo de confirmação
            Dialog confirmationDialog = new Dialog();
            confirmationDialog.setCloseOnEsc(false);

            // Configuração do diálogo de confirmação
            Button confirmButton = new Button("Confirmar", event -> {
                try {
                    // Obter todos os medidores relacionados ao usuário
                    List<EnergyMeter> relatedMeters = energyMeterRepository.findAll().stream()
                            .filter(meter -> meter.getUser() != null && meter.getUser().getUserId().equals(selectedUser.getUserId()))
                            .collect(Collectors.toList());

                    // Para cada medidor relacionado, obter dispositivos e excluir análises e relatórios
                    relatedMeters.forEach(meter -> {
                        List<Device> relatedDevices = deviceRepository.findAll().stream()
                                .filter(device -> device.getEnergyMeter() != null && device.getEnergyMeter().getEnergyMeterId().equals(meter.getEnergyMeterId()))
                                .collect(Collectors.toList());

                        // Para cada dispositivo, obter e excluir análises e relatórios
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
                    });

                    // Excluir os medidores relacionados
                    energyMeterRepository.deleteAll(relatedMeters);

                    // Remova o usuário
                    userService.deleteUser(selectedUser.getUserId());
                    userGrid.setItems(userService.getAllUsers());
                    clearForm();
                    Notification.show("Usuário e elementos relacionados excluídos com sucesso!", 1000, Notification.Position.TOP_START);
                } catch (RuntimeException e) {
                    Notification.show("Erro ao excluir o usuário: " + e.getMessage(), 1000, Notification.Position.TOP_START);
                }
                confirmationDialog.close();
            });

            Button cancelButton = new Button("Cancelar", event -> confirmationDialog.close());

            // Adiciona componentes ao diálogo de confirmação
            confirmationDialog.add(
                    new VerticalLayout(
                            new NativeLabel("Tem certeza de que deseja excluir este usuário? Esta ação removerá medidores, dispositivos, análises e relatórios relacionados."),
                            new HorizontalLayout(confirmButton, cancelButton)
                    )
            );

            confirmationDialog.open();
        } else {
            Notification.show("Selecione um usuário para excluir.", 1000, Notification.Position.TOP_START);
        }
    }


    private void clearForm() {
        selectedUser = null;
        emailField.clear();
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
    }
}
