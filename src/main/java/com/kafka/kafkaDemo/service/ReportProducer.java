package com.kafka.kafkaDemo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.kafkaDemo.controller.Request.MISReportRequest;
import com.kafka.kafkaDemo.controller.Response.ReportResponseStore;
import com.kafka.kafkaDemo.controller.Response.ReportStatusMessage;
import com.kafka.kafkaDemo.controller.Response.ReportStatusStore;
import com.kafka.kafkaDemo.domain.MISReport;
import com.kafka.kafkaDemo.repository.MISRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ReportProducer {

    @Autowired
    private MISRepository misRepository;


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    @KafkaListener(topics = "report-response", groupId = "mis-report-group")
    public void consumeResponse(String message) {
        System.out.println("✅ Message Received from consumer Report Status -> " + message);

    }


    public void publishReportStatus(String reportId, boolean generated) {
        String message = "ReportId=" + reportId + ", generated=" + generated;
        kafkaTemplate.send("report-status-topic", message);
        System.out.println("✅ Published: " + message);
    }

    public void publishReportStatus(ReportStatusMessage statusMessage) {
        kafkaTemplate.send("report-status-topic", statusMessage.toString()); // Or JSON serialize
        System.out.println("✅ Published: " + statusMessage);
    }


    public String genrateReport(MISReportRequest misReportRequest) {
        String reportId = UUID.randomUUID().toString();

        // Convert Request → Entity
        ObjectMapper objectMapper = new ObjectMapper();
        MISReport misReport = objectMapper.convertValue(misReportRequest, MISReport.class);
        misReport.setReportId(reportId);
        misReport.setStatus("IN_PROGRESS");
        misReport.setMessage("Report request submitted.");
        misRepository.save(misReport); // save initial

        new Thread(() -> {
            try {
                // Step 1: Mark report as "IN_PROGRESS"
                publishReportStatus(new ReportStatusMessage(reportId, "IN_PROGRESS", "Report generation started for " + misReportRequest.getReportName(),
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

                        // SUCCESS
                        String reportFileUrl = "http://localhost:8080/reports/" +
                                misReportRequest.getReportName().replace(" ", "_") + ".pdf";

                        ReportStatusMessage successMsg = new ReportStatusMessage(
                                reportId, "SUCCESS",
                                "Report generated successfully at " + reportFileUrl,
                                true
                        );

                        publishReportStatus(successMsg);
                        ReportStatusStore.updateStatus(reportId, successMsg);

                        // 🔹 Update DB
                        misReport.setStatus("SUCCESS");
                        misReport.setMessage("");
                        misReport.setUrl(reportFileUrl);
                        misRepository.save(misReport);

                        success = true;

                    } catch (Exception ex) {
                        ReportStatusMessage failAttempt = new ReportStatusMessage(
                                reportId, "FAILED_ATTEMPT",
                                "Attempt " + attempt + " failed: " + ex.getMessage(),
                                false
                        );
                        publishReportStatus(failAttempt);
                        ReportStatusStore.updateStatus(reportId, failAttempt);

                        // 🔹 Update DB for attempt failure
                        misReport.setStatus("FAILED_ATTEMPT");
                        misReport.setMessage("");
                        misRepository.save(misReport);

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
                    publishReportStatus(finalFail);
                    ReportStatusStore.updateStatus(reportId, finalFail);

                    // 🔹 Update DB final failure
                    misReport.setStatus("FAILED_FINAL");
                    misReport.setMessage("");
                    misRepository.save(misReport);
                }

            } catch (InterruptedException e) {
                ReportStatusMessage interrupted = new ReportStatusMessage(
                        reportId, "INTERRUPTED",
                        "Report generation interrupted: " + e.getMessage(),
                        false
                );
                publishReportStatus(interrupted);
                ReportStatusStore.updateStatus(reportId, interrupted);

                // 🔹 Update DB interruption
                misReport.setStatus("INTERRUPTED");
                misReport.setStatus("");
                misRepository.save(misReport);

                Thread.currentThread().interrupt();
            }
        }).start();

        return "Report request submitted. ReportId=" + reportId;
    }



//    public String genrateReport(MISReportRequest misReportRequest) {
//        String reportId = UUID.randomUUID().toString();
//
//        // Convert Request → Entity
//        ObjectMapper objectMapper = new ObjectMapper();
//        MISReport misReport = objectMapper.convertValue(misReportRequest, MISReport.class);
//        misReport.setReportId(reportId);
//        misReport.setStatus("IN_PROGRESS");
//        misReport.setMessage("Report request submitted.");
//        misRepository.save(misReport);
//
//        // Create future for response
//        CompletableFuture<String> future = new CompletableFuture<>();
//        ReportResponseStore.register(reportId, future);
//
//        // Publish request (async work in a thread)
//        publishReportStatus(new ReportStatusMessage(
//                reportId, "IN_PROGRESS",
//                "Report generation started for " + misReportRequest.getReportName(),
//                false
//        ));
//
//        try {
//            String kafkaResponse = future.get(30, TimeUnit.SECONDS);
//            misReport.setStatus("COMPLETED");
//            misReport.setMessage(kafkaResponse);
//            misRepository.save(misReport);
//
//            return "Final response for reportId=" + reportId + " -> " + kafkaResponse;
//
//        } catch (TimeoutException e) {
//            return "⏳ Timeout waiting for response for reportId=" + reportId;
//        } catch (Exception e) {
//            return "❌ Error while waiting for response: " + e.getMessage();
//        }
//    }

}