package br.com.fiap.energyapi.domain.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report createReport(Report report) {
        return reportRepository.save(report);
    }

    public void saveReport(Report report) {
        reportRepository.save(report);
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Optional<Report> getReportById(Long reportId) {
        return reportRepository.findById(reportId);
    }

    public Report updateReport(Long reportId, Report reportDetails) {
        return reportRepository.findById(reportId)
                .map(report -> {
                    report.setDeviceAnalysis(reportDetails.getDeviceAnalysis());
                    report.setGeneratedAt(reportDetails.getGeneratedAt());
                    return reportRepository.save(report);
                })
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }

    public void deleteReport(Long reportId) {
        reportRepository.deleteById(reportId);
    }
}
