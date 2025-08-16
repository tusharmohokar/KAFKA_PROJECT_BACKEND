package com.kafka.kafkaDemo.controller;

import com.kafka.kafkaDemo.controller.Request.MISReportRequest;
import com.kafka.kafkaDemo.controller.Response.ReportStatusMessage;
import com.kafka.kafkaDemo.controller.Response.ReportStatusStore;
import com.kafka.kafkaDemo.service.ReportProducer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ReportController {
    private final ReportProducer reportProducer;

    public ReportController(ReportProducer reportProducer) {
        this.reportProducer = reportProducer;
    }

    @GetMapping("/generateReport")
    public String generateReport() {
        String reportId = UUID.randomUUID().toString();

        new Thread(() -> {
            try {
                Thread.sleep(500000); // simulate report generation
                reportProducer.publishReportStatus(reportId, true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        return "Report request submitted. ReportId=" + reportId;
    }


    @PostMapping("/post-generateReport")
    public String generateReportNew(@RequestBody MISReportRequest misReportRequest) {
        String reportId = UUID.randomUUID().toString();

        new Thread(() -> {
            try {
                // Step 1: Mark report as "IN_PROGRESS"
                reportProducer.publishReportStatus(new ReportStatusMessage(
                        reportId, "IN_PROGRESS",
                        "Report generation started for " + misReportRequest.getReportName(),
                        false
                ));

                int maxRetries = 5;
                int attempt = 0;
                boolean success = false;

                while (attempt < maxRetries && !success) {
                    attempt++;
                    try {
                        Thread.sleep(3000);

                        if (Math.random() < 0.5) {
                            throw new RuntimeException("Temporary error on attempt " + attempt);
                        }

                        String reportFileUrl = "http://localhost:8080/reports/" +
                                misReportRequest.getReportName().replace(" ", "_") + ".pdf";

                        // SUCCESS
                        ReportStatusMessage successMsg = new ReportStatusMessage(
                                reportId, "SUCCESS",
                                "Report generated successfully at " + reportFileUrl,
                                true
                        );
                        reportProducer.publishReportStatus(successMsg);
                        ReportStatusStore.updateStatus(reportId, successMsg); // store it
                        success = true;

                    } catch (Exception ex) {
                        ReportStatusMessage failAttempt = new ReportStatusMessage(
                                reportId, "FAILED_ATTEMPT",
                                "Attempt " + attempt + " failed: " + ex.getMessage(),
                                false
                        );
                        reportProducer.publishReportStatus(failAttempt);
                        ReportStatusStore.updateStatus(reportId, failAttempt);

                        if (attempt < maxRetries) {
                            Thread.sleep(1000 * attempt);
                        }
                    }
                }

                if (!success) {
                    ReportStatusMessage finalFail = new ReportStatusMessage(
                            reportId, "FAILED_FINAL",
                            "Report generation failed after " + maxRetries + " attempts.",
                            false
                    );
                    reportProducer.publishReportStatus(finalFail.toString(), false);
                    ReportStatusStore.updateStatus(reportId, finalFail);
                }

            } catch (InterruptedException e) {
                ReportStatusMessage interrupted = new ReportStatusMessage(
                        reportId, "INTERRUPTED",
                        "Report generation interrupted: " + e.getMessage(),
                        false
                );
                reportProducer.publishReportStatus(interrupted.getStatus(), false);
                ReportStatusStore.updateStatus(reportId, interrupted);

                Thread.currentThread().interrupt();
            }
        }).start();

        return "Report request submitted. ReportId=" + reportId;
    }


}