package ca.etsmtl.taf.performance.gatling.services;

import ca.etsmtl.taf.performance.gatling.config.GatlingConfigurator;
import ca.etsmtl.taf.performance.gatling.model.GatlingTestResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

@Service
public class ReportService {
    private final String REPORT_PATH = "/team3/reports/performance/gatling/dashboard/";

    public GatlingTestResult getLatestReportResult() throws IOException {
        File reportsDirFile = getReportsDir();
        File latestReportDir = findLatestReportDirectory(reportsDirFile);
        if (latestReportDir != null) {
            File reportFile = new File(latestReportDir, "js/assertions.json");
            if (reportFile.exists()) {
                return buildReportResult(reportFile);
            } else {
                throw new RuntimeException("Aucun rapport de résultats n’a été trouvé dans le dernier répertoire: " + latestReportDir.getName());
            }
        } else {
            throw new RuntimeException("Aucun répertoire de rapport trouvé.");
        }
    }

    public String getLatestReportPath() {
        File reportsDirFile = getReportsDir();
        File latestReportDir = findLatestReportDirectory(reportsDirFile);
        if (latestReportDir != null) {
            File reportFile = new File(latestReportDir, "index.html");
            if (reportFile.exists()) {
                return buildReportPath(latestReportDir);
            } else {
                throw new RuntimeException("Aucun rapport trouvé dans le dernier répertoire.");
            }
        } else {
            throw new RuntimeException("Aucun répertoire de rapport trouvé.");
        }
    }

    private File getReportsDir() {
        Path reportsDir = Paths.get(GatlingConfigurator.getGatlingResultsFolder()).toAbsolutePath().normalize();
        File reportsDirFile = reportsDir.toFile();
        if (!reportsDirFile.exists() || !reportsDirFile.isDirectory()) {
            throw new RuntimeException("Le répertoire des résultats Gatling n'existe pas.");
        }
        return reportsDirFile;
    }

    private File findLatestReportDirectory(File reportsDirFile) {
        return Arrays.stream(reportsDirFile.listFiles(File::isDirectory))
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
    }

    private String buildReportPath(File latestReportDir) {
        return REPORT_PATH + latestReportDir.getName() + "/index.html";
    }

    private GatlingTestResult buildReportResult(File resultFile) throws IOException {
        try {
            return new ObjectMapper().readValue(resultFile, GatlingTestResult.class);
        } catch (IOException e) {
            throw new IOException("Failed to parse Gatling JSON report: " + resultFile.getAbsolutePath(), e);
        }
    }

}
