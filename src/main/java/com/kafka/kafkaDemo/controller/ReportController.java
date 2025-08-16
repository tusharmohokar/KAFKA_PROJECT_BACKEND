package com.kafka.kafkaDemo.controller;

import com.kafka.kafkaDemo.controller.Request.MISReportRequest;
import com.kafka.kafkaDemo.service.ReportProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ReportController {

    @Autowired
    public ReportProducer reportProducer;

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
        return reportProducer.genrateReport(misReportRequest);
    }


}