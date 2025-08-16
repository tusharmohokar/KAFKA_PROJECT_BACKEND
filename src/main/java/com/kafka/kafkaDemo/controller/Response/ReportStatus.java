package com.kafka.kafkaDemo.controller.Response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportStatus {
    private String reportId;
    private String statusMessage;

    public ReportStatus(String statusMessage, String reportId) {
        this.statusMessage = statusMessage;
        this.reportId = reportId;
    }
}
