package com.kafka.kafkaDemo.service;

import com.kafka.kafkaDemo.controller.Response.ReportStatusMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReportProducer {


    private final KafkaTemplate<String, String> kafkaTemplate;

    public ReportProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
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


}