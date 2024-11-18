package br.com.fiap.energyapi.views;

import br.com.fiap.energyapi.domain.report.Report;
import br.com.fiap.energyapi.domain.report.ReportService;
import br.com.fiap.energyapi.domain.user.User;
import br.com.fiap.energyapi.domain.user.UserRepository;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysis;
import br.com.fiap.energyapi.domain.deviceAnalysis.DeviceAnalysisRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("vaadin-reports")
public class ReportView extends VerticalLayout {

    private final ReportService reportService;
    private final UserRepository userRepository;
    private final DeviceAnalysisRepository deviceAnalysisRepository;
    private final Grid<Report> reportGrid = new Grid<>(Report.class);

    private final TextField generatedAtField = new TextField("Data de Geração");
    private final TextField deviceAnalysisIdField = new TextField("ID da Análise de Dispositivo");
    private final TextField userIdField = new TextField("ID do Usuário");
    private Report selectedReport;

    @Autowired
    public ReportView(ReportService reportService, UserRepository userRepository, DeviceAnalysisRepository deviceAnalysisRepository) {
        this.reportService = reportService;
        this.userRepository = userRepository;
        this.deviceAnalysisRepository = deviceAnalysisRepository;

        add(new com.vaadin.flow.component.html.H1("Gerenciamento de Relatórios"));

        reportGrid.setColumns("reportId", "generatedAt", "user.userId");
        reportGrid.setItems(reportService.getAllReports());
        reportGrid.asSingleSelect().addValueChangeListener(event -> selectReport(event.getValue()));

        Button saveButton = new Button("Salvar Relatório");
        saveButton.addClickListener(e -> saveReport());

        Button deleteButton = new Button("Excluir Relatório");
        deleteButton.addClickListener(e -> deleteReport());

        add(reportGrid, generatedAtField, deviceAnalysisIdField, userIdField, saveButton, deleteButton);
    }

    private void selectReport(Report report) {
        if (report != null) {
            selectedReport = report;
            generatedAtField.setValue(report.getGeneratedAt());
            deviceAnalysisIdField.setValue(report.getDeviceAnalysis() != null ? String.valueOf(report.getDeviceAnalysis().getDeviceAnalysisId()) : "");
            userIdField.setValue(report.getUser() != null ? String.valueOf(report.getUser().getUserId()) : "");
        } else {
            clearForm();
        }
    }

    private void saveReport() {
        if (generatedAtField.isEmpty() || deviceAnalysisIdField.isEmpty() || userIdField.isEmpty()) {
            Notification.show("Por favor, preencha todos os campos.");
            return;
        }

        if (selectedReport == null) {
            selectedReport = new Report();
        }
        selectedReport.setGeneratedAt(generatedAtField.getValue());

        try {
            String userId = userIdField.getValue();
            User user = userRepository.findById(userId).orElseThrow(() ->
                    new RuntimeException("Usuário não encontrado com o ID: " + userId));
            selectedReport.setUser(user);

            Long deviceAnalysisId = Long.valueOf(deviceAnalysisIdField.getValue());
            DeviceAnalysis deviceAnalysis = deviceAnalysisRepository.findById(deviceAnalysisId).orElseThrow(() ->
                    new RuntimeException("Análise de dispositivo não encontrada com o ID: " + deviceAnalysisId));
            selectedReport.setDeviceAnalysis(deviceAnalysis);

            // Logging the information before saving
            System.out.println("Relatório sendo salvo:");
            System.out.println("Generated At: " + selectedReport.getGeneratedAt());
            System.out.println("User ID: " + selectedReport.getUser().getUserId());
            System.out.println("Device Analysis ID: " + selectedReport.getDeviceAnalysis().getDeviceAnalysisId());

            reportService.saveReport(selectedReport);
            reportGrid.setItems(reportService.getAllReports());
            clearForm();
            Notification.show("Relatório salvo com sucesso!");
        } catch (NumberFormatException e) {
            Notification.show("ID inválido. Por favor, insira valores numéricos para os campos de ID.");
        } catch (RuntimeException e) {
            Notification.show(e.getMessage());
        }
    }

    private void deleteReport() {
        if (selectedReport != null) {
            reportService.deleteReport(selectedReport.getReportId());
            reportGrid.setItems(reportService.getAllReports());
            clearForm();
            Notification.show("Relatório excluído com sucesso!");
        } else {
            Notification.show("Selecione um relatório para excluir.");
        }
    }

    private void clearForm() {
        selectedReport = null;
        generatedAtField.clear();
        deviceAnalysisIdField.clear();
        userIdField.clear();
    }
}
