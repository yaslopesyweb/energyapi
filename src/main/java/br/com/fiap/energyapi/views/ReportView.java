package br.com.fiap.energyapi.views;

import br.com.fiap.energyapi.domain.report.Report;
import br.com.fiap.energyapi.domain.report.ReportService;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysis;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysisRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

@Route("vaadin-reports")
public class ReportView extends VerticalLayout {

    private final ReportService reportService;
    private final Grid<Report> reportGrid = new Grid<>(Report.class);

    private final ComboBox<DeviceAnalysis> deviceAnalysisComboBox = new ComboBox<>("Análise de Dispositivo");
    private Report selectedReport;

    @Autowired
    public ReportView(ReportService reportService, DeviceAnalysisRepository deviceAnalysisRepository) {
        this.reportService = reportService;

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

        add(new com.vaadin.flow.component.html.H2("Gerenciamento de Relatórios"));

        // Configuração do ComboBox para seleção de análises de dispositivos
        List<DeviceAnalysis> deviceAnalyses = deviceAnalysisRepository.findAll(); // Carregar todas as análises disponíveis
        deviceAnalysisComboBox.setItems(deviceAnalyses);
        deviceAnalysisComboBox.setItemLabelGenerator(da -> "ID: " + da.getDeviceAnalysisId() + " - " + da.getDevice().getDeviceName());
        deviceAnalysisComboBox.setPlaceholder("Selecione uma análise");

        // Configuração do Grid
        reportGrid.setColumns("reportId", "deviceAnalysis.deviceAnalysisId", "generatedAt", "user.userId");
        reportGrid.setItems(reportService.getAllReports());
        reportGrid.asSingleSelect().addValueChangeListener(event -> selectReport(event.getValue()));

        // Botões de ação com estilização
        Button saveButton = new Button("Salvar");
        applyButtonStyles(saveButton);
        Button deleteButton = new Button("Excluir");
        deleteButton.getStyle().set("background-color", "white").set("color", "red");

        saveButton.addClickListener(e -> saveReport());
        deleteButton.addClickListener(e -> deleteReport());

        // Ajuste do ComboBox com estilização
        applyComboBoxStyles(deviceAnalysisComboBox);

        // Centralizando Botões e Componentes
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Adicionando componentes à interface
        add(deviceAnalysisComboBox, saveButton, deleteButton, reportGrid);
    }

    private void selectReport(Report report) {
        if (report != null) {
            selectedReport = report;
            deviceAnalysisComboBox.setValue(report.getDeviceAnalysis());
        } else {
            clearForm();
        }
    }

    private void saveReport() {
        if (deviceAnalysisComboBox.isEmpty()) {
            Notification.show("Por favor, selecione uma análise.", 1000, Notification.Position.TOP_START);
            return;
        }

        if (selectedReport == null) {
            selectedReport = new Report();
            selectedReport.setGeneratedAt(Instant.now().toString()); // Data de geração automática
        }

        try {
            DeviceAnalysis deviceAnalysis = deviceAnalysisComboBox.getValue();
            selectedReport.setDeviceAnalysis(deviceAnalysis);
            selectedReport.setUser(deviceAnalysis.getUser()); // Injeta automaticamente o usuário associado à análise

            reportService.saveReport(selectedReport);
            reportGrid.setItems(reportService.getAllReports());
            clearForm();
            Notification.show("Relatório salvo com sucesso!", 1000, Notification.Position.TOP_START);
        } catch (RuntimeException e) {
            Notification.show(e.getMessage(), 1000, Notification.Position.TOP_START);
        }
    }

    private void deleteReport() {
        if (selectedReport != null) {
            reportService.deleteReport(selectedReport.getReportId());
            reportGrid.setItems(reportService.getAllReports());
            clearForm();
            Notification.show("Relatório excluído com sucesso!", 1000, Notification.Position.TOP_START);
        } else {
            Notification.show("Selecione um relatório para excluir.", 1000, Notification.Position.TOP_START);
        }
    }

    private void clearForm() {
        selectedReport = null;
        deviceAnalysisComboBox.clear();
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




















