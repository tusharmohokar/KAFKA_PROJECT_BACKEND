package com.kafka.kafkaDemo.service;
import com.kafka.kafkaDemo.controller.Response.ReportStatus;
import com.kafka.kafkaDemo.controller.Response.ReportStatusStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReportConsumer {


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "report-status-topic", groupId = "mis-report-group")
    public void consume(String message) {
        System.out.println("📩 Raw Kafka Message: " + message);

        try {
            String[] parts = message.split(":::");
            String reportId = parts.length > 0 ? parts[0] : "UNKNOWN";
            String statusMessage = parts.length > 1 ? parts[1] : "No details";

            ReportStatus reportStatus = new ReportStatus(reportId, statusMessage);
            System.out.println("✅ Processed Report Status -> " + reportStatus);

            kafkaTemplate.send("report-response","generated");

        } catch (Exception e) {
            System.err.println("❌ Failed to process Kafka message: " + message);
            e.printStackTrace();
        }
    }

}