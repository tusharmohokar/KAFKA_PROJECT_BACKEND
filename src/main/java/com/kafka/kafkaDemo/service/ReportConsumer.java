package com.kafka.kafkaDemo.service;
import com.kafka.kafkaDemo.controller.Response.ReportStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ReportConsumer {
    @KafkaListener(topics = "report-status-topic", groupId = "mis-report-group")
    public void consume(String message) {
        System.out.println("📩 Raw Kafka Message: " + message);

        try {
            // Step 1: Parse the message (expected format: reportId:::statusMessage)
            String[] parts = message.split(":::");
            String reportId = parts.length > 0 ? parts[0] : "UNKNOWN";
            String statusMessage = parts.length > 1 ? parts[1] : "No details";

            // Step 2: Build a status object
            ReportStatus reportStatus = new ReportStatus(reportId, statusMessage);

            // Step 3: Save status (in DB or cache - here we use in-memory for demo)
//            ReportStatusStore.updateStatus(reportId, reportStatus);

            // Step 4: Print for visibility
            System.out.println("✅ Processed Report Status -> " + reportStatus);

        } catch (Exception e) {
            System.err.println("❌ Failed to process Kafka message: " + message);
            e.printStackTrace();
        }
    }

}